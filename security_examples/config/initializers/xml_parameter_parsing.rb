require 'action_dispatch/xml_params_parser'

SecurityExamples::Application.config.middleware.insert_before Rack::Head, ActionDispatch::XmlParamsParser
