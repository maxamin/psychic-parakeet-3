Rails.application.routes.draw do
  resources :users, only: [:index]
  resources :sessions, only: [:new, :create, :destroy]

  match '/signin',  to: 'sessions#new',         via: 'get'
  match '/signout', to: 'sessions#destroy',     via: 'get'

  root to: 'sessions#new'
end
