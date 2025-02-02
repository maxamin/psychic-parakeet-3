require_relative '../app.rb'
require 'rspec'
require 'rack/test'

set :environment, :test

describe 'Server Service' do
  include Rack::Test::Methods

  def app
    Sinatra::Application
  end

  it "should load the page" do
    get '/'
    expect(last_response).to be_ok
    expect(last_response.body).to include("Hello world")
  end
end
