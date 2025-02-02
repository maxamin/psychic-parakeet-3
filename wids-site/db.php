<?php

# This function reads your DATABASE_URL config var and returns a connection
# string suitable for pg_connect. Put this in your app.
# lifted from: https://github.com/kch/heroku-php-pg/blob/master/index.php
function pg_connection_string_from_database_url()
{
  extract(parse_url($_ENV["DATABASE_URL"]));
  return "user=$user password=$pass host=$host dbname=" . substr($path, 1); # <- you may want to add sslmode=require there too
}

pg_connect(pg_connection_string_from_database_url());
