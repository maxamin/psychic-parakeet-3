class mysql {
  $pw = "mysql"

  package { "mysql-server":
    ensure => present,
    require => Exec["apt-get update"]
  }

  service { "mysql":
    ensure => running,
    require => Package["mysql-server"],
  }

  exec { "mysqlpw":
    unless => "mysqladmin -uroot -p$pw status",
    command => "mysqladmin -uroot password $pw",
    require => Service["mysql"],
  }
}