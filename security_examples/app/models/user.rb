class User < ApplicationRecord
  validates_presence_of :name

  def to_xml(options={})
    if options[:builder]
      options[:builder].name name
    else
      "<name>#{name}</name>"
    end
  end
end
