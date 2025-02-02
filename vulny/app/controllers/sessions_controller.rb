class SessionsController < ApplicationController
  def create
    username = Base64.decode64(params[:session][:username])
    password = params[:session][:password]

    # First check to make sure the user exists
    if User.find_by(username: username)
      # If the user exists, check to see if the password is correct, if it is sign the user in.
      if user = User.find_by_sql("SELECT * FROM users WHERE username = '#{ username }' AND password = '#{ password }'").first
        sign_in user
        redirect_to controller: 'users', action: 'index'
      else
        # Show an error that the username and password combination were incorrect
        flash[:error] = "Invalid username/password combination"
        redirect_to root_path
      end
    else
      # Show an error that there is no such username
      flash[:error] = "User #{ username } not found"
      redirect_to root_path
    end
  end

  def destroy
    sign_out
    flash[:notice] = "You have been signed out."
    redirect_to root_path
  end
end
