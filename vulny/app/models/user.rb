class User < ActiveRecord::Base
  before_create :create_remember_token

  def User.new_remember_token
    SecureRandom.hex
  end

  private

    def create_remember_token
      self.remember_token = User.new_remember_token
    end
end
