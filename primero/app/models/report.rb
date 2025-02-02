# frozen_string_literal: true

# Configurable reports for aggregating data over Primero records.
# rubocop:disable Metrics/ClassLength
class Report < ApplicationRecord
  include LocalizableJsonProperty
  include ConfigurationRecord

  REPORTABLE_FIELD_TYPES = [
    # Field::TEXT_FIELD,
    # Field::TEXT_AREA,
    Field::RADIO_BUTTON,
    Field::SELECT_BOX,
    Field::NUMERIC_FIELD,
    Field::DATE_FIELD,
    # Field::DATE_RANGE,
    Field::TICK_BOX,
    Field::TALLY_FIELD
  ].freeze

  AGGREGATE_COUNTS_FIELD_TYPES = [
    Field::NUMERIC_FIELD,
    Field::TALLY_FIELD
  ].freeze

  DAY = 'date' # eg. 13-Jan-2015
  WEEK = 'week' # eg. Week 2 Jan-2015
  MONTH = 'month' # eg. Jan-2015
  YEAR = 'year' # eg. 2015
  DATE_RANGES = [DAY, WEEK, MONTH, YEAR].freeze

  localize_properties :name, :description

  # TODO: Currently it's not worth trying to save off the report data.
  #      The report builds a value hash with an array of strings as keys. CouchDB/CouchRest converts this array to
  #      a string. Not clear what benefit could be gained by storing the data but converting keys to strings on the fly
  #      when rendering the graph and table. So for now we will rebuild the data.
  attr_accessor :data
  attr_accessor :add_default_filters
  attr_accessor :aggregate_by_ordered
  attr_accessor :disaggregate_by_ordered
  attr_accessor :permission_filter
  self.unique_id_from_attribute = 'name_en'

  alias_attribute :graph, :is_graph

  validates :record_type, presence: true
  validates :aggregate_by, presence: true
  validate :modules_present
  validate :validate_name_in_base_language

  before_create :generate_unique_id
  before_save :apply_default_filters

  def validate_name_in_base_language
    return if name_en.present?

    errors.add(:name, I18n.t('errors.models.report.name_presence'))
  end

  class << self
    def get_reportable_subform_record_field_name(model, record_type)
      model = Record.model_from_name(model)
      return unless model.try(:nested_reportable_types)

      model.nested_reportable_types.select { |nrt| nrt.model_name.param_key == record_type }.first&.record_field_name
    end

    def get_reportable_subform_record_field_names(model)
      model = Record.model_from_name(model)
      return unless model.try(:nested_reportable_types)

      model.nested_reportable_types.map { |nrt| nrt.model_name.param_key }
    end

    def record_type_is_nested_reportable_subform?(model, record_type)
      get_reportable_subform_record_field_names(model).include?(record_type)
    end

    def all_nested_reportable_types
      record_types = []
      FormSection::RECORD_TYPES.each do |rt|
        record_types += Record.model_from_name(rt).try(:nested_reportable_types)
      end
      record_types
    end

    def new_with_properties(report_params)
      report = Report.new(report_params.except(:name, :description, :fields))
      report.name_i18n = report_params[:name]
      report.description_i18n = report_params[:description]
      report.aggregate_by = ReportFieldService.aggregate_by_from_params(report_params)
      report.disaggregate_by = ReportFieldService.disaggregate_by_from_params(report_params)
      report
    end
  end

  def update_properties(report_params)
    report_params = report_params.with_indifferent_access if report_params.is_a?(Hash)
    converted_params = FieldI18nService.convert_i18n_properties(Report, report_params)
    merged_props = FieldI18nService.merge_i18n_properties(attributes, converted_params)
    assign_attributes(report_params.except(:name, :description, :fields).merge(merged_props))
    return unless report_params[:fields]

    self.aggregate_by = ReportFieldService.aggregate_by_from_params(report_params)
    self.disaggregate_by = ReportFieldService.disaggregate_by_from_params(report_params)
  end

  def modules
    @modules ||= PrimeroModule.all(keys: [module_id]).all if module_id.present?
  end

  def field_map
    @pivot_fields
  end

  # This method transforms the current values format: {["child_mother", "female"] => 1}
  # to a nested hash: { "child_mother" => { "female" =>{ "_total" => 1 } } }
  def values_as_json_hash
    values_tree = {}
    values
      .select { |k, _| k.select { |e| e.to_s.present? }.present? } # Remove empty arrays ["", ""]
      .each { |key, total| values_tree = build_values_tree(values_tree, key, total) }
    values_tree
  end

  def build_values_tree(values_tree, key, total)
    key.each_with_index do |key_value, index|
      values_tree = values_for_key(values_tree, key_value, key, total, index)
    end
    values_tree
  end

  def values_for_key(values_tree, key_value, key, total, index)
    new_value = key_value == key.last ? { key_value => { '_total' => total } } : { key_value => {} }
    values_tree = new_value if values_tree.blank?
    if index.zero?
      values_tree = values_tree.merge(new_value) unless key_at?(values_tree, [], key_value)
    elsif key_value.to_s.blank?
      non_empty_values_as_parent_key(values_tree, key, total)
    elsif !key_at?(values_tree, key[0..(index - 1)], key_value)
      set_for_parents(values_tree, key[0..(index - 1)], new_value)
    end
    values_tree
  end

  def non_empty_values_as_parent_key(values_tree, key, total)
    parent_keys = key.select { |k| k.to_s.present? }
    set_for_parents(values_tree, parent_keys, '_total' => total)
  end

  def set_for_parents(tree, parents, value)
    next_parents = parents.dup
    parent_key = next_parents.shift
    if next_parents.blank?
      tree[parent_key] = tree[parent_key].present? ? tree[parent_key].merge(value) : value
    else
      set_for_parents(tree[parent_key], next_parents, value)
    end
  end

  def tree_for_parents(tree, parents)
    next_parents = parents.dup
    parents.present? && tree.present? ? tree_for_parents(tree[next_parents.shift], next_parents.dup) : tree
  end

  def key_at?(tree, parents, key)
    tree = tree_for_parents(tree, parents)
    tree.present? ? tree.key?(key) : false
  end

  # Run the Solr query that calculates the pivots and format the output.
  # rubocop:disable Metrics/AbcSize
  def build_report
    # Prepopulates pivot fields
    pivot_fields
    return if pivots.blank?

    self.values = report_values(record_type, pivots, filters)
    process_aggregate_counts if aggregate_counts_from.present?
    group_by_ages
    group_by_dates(pivot_fields, group_dates_by) if group_dates_by.present?
    self.data = report_data
    ''
  end
  # rubocop:enable Metrics/AbcSize

  def report_data
    aggregate_limit = aggregate_by.size
    aggregate_limit = dimensionality if aggregate_limit > dimensionality

    {
      aggregate_value_range: aggregate_range(aggregate_limit),
      disaggregate_value_range: disaggregate_range(aggregate_limit),
      values: @values
    }
  end

  def aggregate_range(aggregate_limit)
    values.keys.map do |pivot|
      pivot[0..(aggregate_limit - 1)]
    end.uniq.compact.sort(&method(:pivot_comparator))
  end

  def disaggregate_range(aggregate_limit)
    values.keys.map do |pivot|
      pivot[aggregate_limit..-1]
    end.uniq.compact.sort(&method(:pivot_comparator))
  end

  def process_aggregate_counts
    if dimensionality < ((aggregate_by + disaggregate_by).size + 1)
      # The numbers are off because a dimension is missing. Zero everything out!
      self.values = values.map { |pivots, _| [pivots, 0] }
    end
    aggregate_counts_from_field = Field.find_by_name(aggregate_counts_from)&.first
    return if aggregate_counts_from_field.blank?

    process_aggregate_counts_from_tally_or_numeric(aggregate_counts_from_field)
  end

  def process_aggregate_counts_from_tally_or_numeric(aggregate_counts_from_field)
    if aggregate_counts_from_field.type == Field::TALLY_FIELD
      process_aggregate_counts_from_tally
    elsif aggregate_counts_from_field.type == Field::NUMERIC_FIELD
      process_aggregate_count_from_numeric
    end
  end

  def process_aggregate_counts_from_tally
    self.values = map_tally_values
    self.values = Reports::Utils.group_values(values, dimensionality - 1) do |pivot_name|
      pivot_name.split(':')[0]
    end
    self.values = Reports::Utils.correct_aggregate_counts(values)
  end

  def process_aggregate_count_from_numeric
    self.values = map_numeric_values
    self.values = Reports::Utils.group_values(values, dimensionality - 1) do |pivot_name|
      pivot_name.is_a?(Numeric) ? '' : pivot_name
    end
    self.values = map_pivot_values
    Reports::Utils.correct_aggregate_counts(values)
  end

  def map_tally_values
    values.map do |pivots, value|
      if pivots.last.present? && pivots.last.match(/\w+:\d+/)
        tally = pivots.last.split(':')
        value *= tally[1].to_i
      end
      [pivots, value]
    end.to_h
  end

  def map_numeric_values
    values.map do |pivots, value|
      if pivots.last.is_a?(Numeric)
        value *= pivots.last
      elsif pivots.last == ''
        value = 0
      end
      [pivots, value]
    end.to_h
  end

  def map_pivot_values
    values.map do |pivots, value|
      pivots = pivots[0..-2] if pivots.last == ''
      [pivots, value]
    end.to_h
  end

  def group_by_ages
    age_ranges = SystemSettings.primary_age_ranges
    pivots.each do |pivot|
      next unless group_ages?(pivot)

      age_field_index = pivot_index(pivot)
      next unless group_ages && age_field_index && age_field_index < dimensionality

      self.values = Reports::Utils.group_values(values, age_field_index) do |pivot_name|
        age_ranges.find { |range| range.cover? pivot_name }
      end
    end
  end

  def group_ages?(pivot)
    /(^age$|^age_.*|.*_age$|.*_age_.*)/.match(pivot) &&
      field_map[pivot].present? &&
      field_map[pivot]['type'] == 'numeric_field'
  end

  def group_by_dates(pivot_fields, group_dates_by)
    date_fields = pivot_fields.select { |_, f| f.type == Field::DATE_FIELD }
    date_fields.each do |field_name, _|
      next unless pivot_index(field_name) < dimensionality

      self.values = Reports::Utils.group_values(values, pivot_index(field_name)) do |pivot_name|
        Reports::Utils.date_range(pivot_name, group_dates_by)
      end
    end
  end

  def modules_present
    if module_id.present? && module_id.length >= 1
      if module_id.split('-').first != 'primeromodule'
        errors.add(:module_id, I18n.t('errors.models.report.module_syntax'))
      end
    else
      errors.add(:module_id, I18n.t('errors.models.report.module_presence'))
    end
  end

  def aggregate_value_range
    data[:aggregate_value_range]
  end

  def disaggregate_value_range
    data[:disaggregate_value_range]
  end

  def values
    # A little contorted to allow report data saving in the future
    if @values.present?
      @values
    elsif data.present?
      data[:values]
    else
      {}
    end
  end

  attr_writer :values

  def dimensionality
    if values.present?
      d = values.first.first.size
    else
      d = (aggregate_by + disaggregate_by).size
      d += 1 if aggregate_counts_from.present?
    end
    d
  end

  # Recursively read through the Solr pivot output and construct a vector of results.
  # The output is an array of arrays (easily convertible into a hash) of the following format:
  # [
  #   [[x0, y0, z0, ...], pivot_count0],
  #   [[x1, y1, z1, ...], pivot_count1],
  #   ...
  # ]
  # where each key is an array of the pivot nest tree, and the value is the aggregate pivot count.
  # So if the Solr pivot query is location, pivoted by protection concern, by age, and by sex,
  # the key array will be:
  #   [[a location, a protection concern, an age, a sex], count of records matching this criteria]
  # Solr returns partial pivot counts. In those cases, the unknown pivot key will be an empty string.
  #   [["Somalia", "CAAFAG", "", ""], count]
  # returns the count of all ages and sexes that are CAFAAG in Somalia
  def value_vector(parent_key, pivots)
    current_key = parent_key + [pivots['value']]
    current_key = [] if current_key == [nil]
    return [[current_key, pivots['count']]] if pivots['pivot'].blank?

    vectors = []
    pivots['pivot'].each { |child| vectors += value_vector(current_key, child) }
    vectors += vectors_for_key(pivots, vectors.first.first.size, current_key)
    vectors
  end

  def vectors_for_key(pivots, max_key_length, current_key)
    this_key = current_key + ([''] * (max_key_length - current_key.length))
    [[this_key, pivots['count']]]
  end

  def self.reportable_record_types
    FormSection::RECORD_TYPES + ['violation'] + Report.all_nested_reportable_types.map { |nrt| nrt.name.underscore }
  end

  def apply_default_filters
    return unless add_default_filters

    self.filters ||= []
    default_filters = Record.model_from_name(record_type).report_filters
    self.filters = (self.filters + default_filters).uniq
  end

  def pivots
    (aggregate_by || []) + (disaggregate_by || [])
  end

  def pivot_fields
    @pivot_fields ||= Field.find_by_name(pivots).group_by(&:name).map { |k, v| [k, v.first] }.to_h
  end

  def pivots_map
    @pivots_map ||= pivots.map { |pivot| [pivot, Field.find_by_name(pivot)&.first] }.to_h
  end

  def pivot_index(field_name)
    pivots.index(field_name)
  end

  def pivot_comparator(compare_a, compare_b)
    (compare_a <=> compare_b) || (compare_a.to_s <=> compare_b.to_s)
  end

  private

  def report_values(record_type, pivots, filters)
    result = {}
    pivots += [aggregate_counts_from] if aggregate_counts_from.present?
    pivots_data = query_solr(record_type, pivots, filters)
    # TODO: The format needs to change and we should probably store data? Although the report seems pretty fast for 100
    result = value_vector([], pivots_data).to_h if pivots_data['pivot'].present?
    result
  end

  # TODO: This method should really be replaced by a Sunspot query
  def query_solr(record_type, pivots, filters)
    # TODO: This has to be valid and open if a case.
    number_of_pivots = pivots.size # can also be dimensionality, but the goal is to move the solr methods out
    pivots_string = pivots.map { |p| SolrUtils.indexed_field_name(record_type, p) }.select(&:present?).join(',')
    filter_query = build_solr_filter_query(record_type, merge_permission_filter(filters))
    mincount = exclude_empty_rows? ? 1 : -1
    result_pivots = if number_of_pivots == 1
                      get_by_field_facet(filter_query, pivots_string, mincount)
                    else
                      get_by_pivot_facet(filter_query, pivots_string, mincount)
                    end
    { 'pivot' => result_pivots }
  end

  def merge_permission_filter(filters)
    return filters if permission_filter.blank?
    return filters + [permission_filter] unless filters_include_permission_filter?

    filters.map do |elem|
      next(elem) unless elem['attribute'] == permission_filter['attribute']

      elem.merge('value' => elem['value'] + permission_filter['value'])
    end
  end

  def filters_include_permission_filter?
    return false unless permission_filter.present?

    filters.any? { |elem| elem['attribute'] == permission_filter['attribute'] }
  end

  def get_by_field_facet(filter_query, pivots_string, mincount)
    response = SolrUtils.sunspot_rsolr.get('select', params: field_params(filter_query, pivots_string, mincount))
    # TODO: A bit of a hack to assume that numeric Solr fields will always end with "_i"
    is_numeric = pivots_string.end_with? '_i'
    result_pivots(response, pivots_string, is_numeric)
  end

  def result_pivots(response, pivots_string, is_numeric)
    result_pivots = []
    response.dig('facet_counts', 'facet_fields', pivots_string)&.each do |v|
      if v.class == String
        result_pivots << (is_numeric ? { 'value' => v.to_i } : { 'value' => v })
      else
        result_pivots.last['count'] = v
      end
    end
    result_pivots
  end

  def get_by_pivot_facet(filter_query, pivots_string, mincount)
    response = SolrUtils.sunspot_rsolr.get('select', params: pivot_params(filter_query, pivots_string, mincount))
    response.dig('facet_counts', 'facet_pivot', pivots_string) || []
  end

  def field_params(filter_query, pivots_string, mincount)
    {
      fq: filter_query,
      start: 0,
      q: '*:*',
      rows: 0,
      facet: 'on',
      'facet.field': pivots_string,
      'facet.mincount': mincount,
      'facet.limit': -1
    }
  end

  def pivot_params(filter_query, pivots_string, mincount)
    {
      fq: filter_query,
      start: 0,
      q: '*:*',
      rows: 0,
      facet: 'on',
      'facet.pivot': pivots_string,
      'facet.pivot.mincount': mincount,
      'facet.limit': -1
    }
  end

  def build_solr_filter_query(record_type, filters)
    filters_query = ["type:#{solr_record_type(record_type)}"]
    filters_query += filters.map { |filter| calculate_filter(filter, record_type) }.compact.flatten if filters.present?
    filters_query
  end

  def calculate_filter(filter, record_type)
    attribute = SolrUtils.indexed_field_name(record_type, filter['attribute'])
    constraint = filter['constraint']
    value = filter['value']
    is_permission_filter = filter['attribute'] == permission_filter&.dig('attribute')
    if attribute.present? && value.present?
      filter_attribute_value(attribute, value, constraint, is_permission_filter)
    elsif attribute.present? && constraint.present? && constraint == 'not_null'
      "#{attribute}:[* TO *]"
    end
  end

  def filter_attribute_value(attribute, value, constraint, is_permission_filter)
    if constraint.present?
      filter_constraint(attribute, value, constraint)
    elsif value.respond_to?(:map) && value.size.positive?
      filter_value(attribute, value, is_permission_filter)
    end
  end

  def filter_constraint(attribute, value, constraint)
    value = Date.parse(value.to_s).strftime('%FT%H:%M:%SZ') unless value.to_s.is_number?
    if constraint == '>'
      "#{attribute}:[#{value} TO *]"
    elsif constraint == '<'
      "#{attribute}:[* TO #{value}]"
    else
      "#{attribute}:\"#{value}\""
    end
  end

  def filter_value(attribute, value, is_permission_filter)
    condition = is_permission_filter ? ' AND ' : ' OR '
    "#{attribute}:(" +
      value.map { |v| v == 'not_null' ? '[* TO *]' : Sunspot::Util.escape(v.to_s) }.join(condition) +
      ')'
  end

  def solr_record_type(record_type)
    record_type = 'child' if record_type == 'case'
    record_type.camelize
  end
end
# rubocop:enable Metrics/ClassLength
