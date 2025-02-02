
class metasploit::postgresql {
  package { 'postgresql':
    ensure => installed
  }
  ->
  exec { 'create metasploit user':
    command => '/usr/bin/sudo -u postgres /bin/bash -l -c "/usr/bin/psql -c \"CREATE USER metasploit WITH PASSWORD \'utdipQueatDuvDav\';\""',
    unless  => '/usr/bin/sudo -u postgres /bin/bash -l -c "/usr/bin/psql postgres -tAc \"SELECT 1 FROM pg_roles WHERE rolname=\'metasploit\'\" | grep -q 1"'
  }
  ->
  exec { 'create metasploit database':
    command => '/usr/bin/sudo -u postgres /bin/bash -l -c "/usr/bin/psql -c \"CREATE DATABASE metasploit OWNER metasploit;\""',
    unless  => '/usr/bin/sudo -u postgres /bin/bash -l -c "/usr/bin/psql postgres -tAc \"SELECT 1 from pg_database WHERE datname=\'metasploit\'\" | grep -q 1"'
  }
}

class metasploit {
  include metasploit::postgresql

  package {['bundler', 'libxml2-dev', 'libxslt1-dev', 'libpq-dev', 'libncurses5-dev', 'ruby-dev', 'libpcap-dev', 'libssl-dev', 'libyaml-dev', 'sudo', 'curl', 'rubygems']:
    ensure => installed
  }
  ->
  user { 'metasploit':
    ensure     => present,
    shell      => '/bin/bash',
    managehome => true,
    home       => '/home/metasploit'
  }
  ->
  exec { 'install rvm gpg key':
    command => '/usr/bin/sudo -u metasploit /usr/bin/gpg --keyserver hkp://keys.gnupg.net --recv-keys 409B6B1796C275462A1703113804BB82D39DC0E3',
    require => User['metasploit']
  }
  ->
  exec { 'install rvm':
    command => '/usr/bin/sudo -u metasploit /usr/bin/curl -L https://get.rvm.io | /usr/bin/sudo -u metasploit /bin/bash -s stable --ruby --autolibs=enable --auto-dotfiles',
    creates => '/home/metasploit/.rvm',
    require => User['metasploit']
  }
  ->
  exec { 'rvm install 1.9.3':
    command => '/usr/bin/sudo -u metasploit /bin/bash -l -c "rvm install 1.9.3-p547"',
    creates => '/home/metasploit/.rvm/rubies/ruby-1.9.3-p547',
  }
  ->
  exec { 'set ruby 1.9.3 as rvm default':
    command => '/usr/bin/sudo -u metasploit /bin/bash -l -c "rvm --default use 1.9.3"',
  }
  ->
  exec { 'install metasploit':
    command => 'sudo -u metasploit /bin/bash -l -c "/usr/bin/git clone https://github.com/mcfakepants/metasploit-framework.git /home/metasploit/metasploit"',
    creates => '/home/metasploit/metasploit',
    timeout => 1800
  }
  ->
  exec { 'install dependencies':
    command => 'sudo -u metasploit /bin/bash -l -c "cd /home/metasploit/metasploit && bundle install"',
  }
  ->
  file { '/home/metasploit/metasploit/config/database.yml':
    ensure  => present,
    owner   => 'metasploit',
    content => template('metasploit/database.yml.erb')
  }
}

