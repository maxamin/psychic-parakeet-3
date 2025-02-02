class java {
package { 'openjdk-7-jre': 
	ensure => installed,
	require => Exec["apt-get update"]

 }
}