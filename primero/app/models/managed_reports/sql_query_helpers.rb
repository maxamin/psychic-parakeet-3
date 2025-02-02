# frozen_string_literal: true

# Helper methods to query the db
module ManagedReports::SqlQueryHelpers
  extend ActiveSupport::Concern

  # ClassMethods
  module ClassMethods
    def equal_value_query(param, table_name = nil)
      return unless param.present?

      ActiveRecord::Base.sanitize_sql_for_conditions(
        ["#{quoted_query(table_name, 'data')} ->> ? = ?", param.field_name, param.value]
      )
    end

    def equal_value_query_multiple(param, table_name = nil, map_to = nil)
      return unless param.present?

      field_name = map_to || param.field_name

      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "#{quoted_query(table_name, 'data')} #> '{#{field_name}}' ?| array[:values]",
          values: param.values
        ]
      )
    end

    def date_range_query(param, table_name = nil)
      return unless param.present?

      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "to_timestamp(#{quoted_query(table_name, 'data')} ->> ?, 'YYYY-MM-DDTHH\\:\\MI\\:\\SS') between ? and ?",
          param.field_name,
          param.from,
          param.to
        ]
      )
    end

    def agency_scope_query(current_user, table_name = nil)
      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "#{quoted_query(table_name, 'data')} #> '{associated_user_agencies}' ?| array[:agencies]",
          agencies: [current_user.agency.unique_id]
        ]
      )
    end

    def group_scope_query(current_user, table_name = nil)
      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "#{quoted_query(table_name, 'data')} #> '{associated_user_groups}' ?| array[:groups]",
          groups: current_user.user_group_unique_ids
        ]
      )
    end

    def self_scope_query(current_user, table_name = nil)
      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "#{quoted_query(table_name, 'data')} #> '{associated_user_names}' ?| array[:user_names]",
          user_names: [current_user.user_name]
        ]
      )
    end

    def quoted_query(table_name, column_name)
      return ActiveRecord::Base.connection.quote_column_name(column_name) if table_name.blank?

      "#{quoted_table_name(table_name)}.#{ActiveRecord::Base.connection.quote_column_name(column_name)}"
    end

    def quoted_table_name(table_name)
      return unless table_name.present?

      ActiveRecord::Base.connection.quote_table_name(table_name)
    end

    def grouped_year_query(date_param, table_name = nil)
      return unless date_param.present?

      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "DATE_PART(
            'year',
            to_timestamp(#{quoted_query(table_name, 'data')} ->> :date_field, 'YYYY-MM-DDTHH\\:\\MI\\:\\SS')
          )::integer",
          date_field: date_param.field_name
        ]
      )
    end

    def grouped_quarter_query(date_param, table_name = nil)
      return unless date_param.present?

      quoted_field = "#{quoted_query(table_name, 'data')} ->> :date_field"

      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "DATE_PART('year', to_timestamp(#{quoted_field}, 'YYYY-MM-DDTHH\\:\\MI\\:\\SS'))|| '-' ||
          'Q' || DATE_PART('quarter', to_timestamp(#{quoted_field}, 'YYYY-MM-DDTHH\\:\\MI\\:\\SS')) ",
          date_field: date_param.field_name
        ]
      )
    end

    def grouped_month_query(date_param, table_name = nil)
      return unless date_param.present?

      quoted_field = "#{quoted_query(table_name, 'data')} ->> :date_field"

      ActiveRecord::Base.sanitize_sql_for_conditions(
        [
          "DATE_PART('year', to_timestamp(#{quoted_field}, 'YYYY-MM-DDTHH\\:\\MI\\:\\SS')) || '-' ||
          to_char(to_timestamp(#{quoted_field}, 'YYYY-MM'),'mm')",
          date_field: date_param.field_name
        ]
      )
    end

    def age_ranges_query(field_name = 'age', table_name = nil, is_json_field = true)
      SystemSettings.primary_age_ranges.reduce("case \n") do |acc, range|
        column = if is_json_field 
                   "#{quoted_query(table_name, 'data')} ->> :field_name" 
                 else 
                   quoted_query(table_name, field_name)
                 end

        acc + ActiveRecord::Base.sanitize_sql_for_conditions(
          [
            %{
              when int4range(:start, :end, '[]') @> cast(#{column} as integer)
              then #{range != SystemSettings.primary_age_ranges.last ? "':start - :end'" : "':start+' end"}
            }, field_name: field_name, start: range.first, end: range.last
          ]
        )
      end
    end
  end
end
