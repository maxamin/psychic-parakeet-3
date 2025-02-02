apt update
apt install -y apt-transport-https ca-certificates
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys 561F9B9CAC40B2F7
echo "deb https://oss-binaries.phusionpassenger.com/apt/passenger xenial main" > /etc/apt/sources.list.d/passenger.list
apt update
apt upgrade -y
apt install -y ruby ruby-dev build-essential postgresql libpq-dev libsqlite3-dev nodejs passenger
gem install --no-ri --no-rdoc bundler
adduser --disabled-password --gecos 'Vulny user' vulny
git clone https://github.com/tomekr/vulny ~vulny/vulny
cd ~vulny/vulny

git checkout ui-change
bundle update

echo -e "default: &default\n  adapter: postgresql\n  encoding: unicode\n  database: vulny\n  pool: 5\n\ndevelopment:\n  <<: *default\n\nproduction:\n  <<: *default" > config/database.yml

mkdir log tmp
chown vulny:vulny log tmp
sudo -u postgres createuser --createdb vulny
sudo -u vulny rake db:setup

echo -e '{\n  "environment":"development",\n  "port":80,\n  "daemonize":true,\n  "user":"vulny"\n}' > Passengerfile.json
sudo -u postgres psql vulny -c "REVOKE ALL PRIVILEGES ON DATABASE vulny FROM vulny;"
sudo -u postgres psql vulny -c "GRANT SELECT ON users, schema_migrations TO vulny;"
sudo -u postgres psql vulny -c "ALTER ROLE vulny WITH NOCREATEDB;"
passenger start
