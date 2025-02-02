# frozen_string_literal: true

require 'rails_helper'

describe ManagedReports::Indicators::IndividualViolationType do
  before do
    clean_data(Incident, Violation, IndividualVictim)

    incident = Incident.create!(data: { incident_date: Date.new(2021, 5, 23), status: 'open' })

    violation1 = Violation.create!(
      data: { type: 'killing', ctfmr_verified: 'verified', violation_tally: { 'boys': 2, 'girls': 0, 'unknown': 2, 'total': 4 } },
      incident_id: incident.id
    )

    violation1.individual_victims = [
      IndividualVictim.create!(
        data: {
          individual_age: 9
        }
      ),

      IndividualVictim.create!(
        data: {
          individual_age: 2
        }
      )
    ]

    violation2 = Violation.create!(
      data: { type: 'attack_on_schools', violation_tally: { 'boys': 2, 'girls': 0, 'unknown': 2, 'total': 4 } },
      incident_id: incident.id
    )

    violation2.individual_victims = [
      IndividualVictim.create!(
        data: {
          individual_age: 10,
          individual_sex: 'unknown'
        }
      )
    ]
  end

  it 'return data for individual age indicator' do
    data = ManagedReports::Indicators::IndividualViolationType.build(
      nil,
      {
        'grouped_by' => SearchFilters::Value.new(field_name: 'grouped_by', value: 'quarter'),
        'incident_date' => SearchFilters::DateRange.new(
          field_name: 'incident_date',
          from: '2021-04-01',
          to: '2022-06-10'
        )
      }
    ).data

    expect(data).to match_array(
      [{ group_id: '2021-Q2', data: [{ id: 'attack_on_schools', total: 1 }, { id: 'killing', total: 2 }] },
       { group_id: '2021-Q3', data: [] },
       { group_id: '2021-Q4', data: [] },
       { group_id: '2022-Q1', data: [] },
       { group_id: '2022-Q2', data: [] }]
    )
  end
end
