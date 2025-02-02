class apache {
  package { "apache2":
    ensure => present,
    require => Exec["apt-get update"]
  }

  # enable file includes via url
  exec { "FileIncludes":
        command => "sed -i 's/allow_url_include = Off/allow_url_include = On/' /etc/php5/apache2/php.ini",
        onlyif  => "grep -c 'allow_url_include = Off' /etc/php5/apache2/php.ini",
        require => Package["apache2"],
  }

  #makes sure apache is running
  service { "apache2":
    ensure => running,
    require => Package["apache2"],
  }
}
