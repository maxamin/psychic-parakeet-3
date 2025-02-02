#DB
Db is a mysql [docker image](https://hub.docker.com/_/mysql)
1. Pull the image: docker pull mysql
2. Run it locally: 
docker run -p 3306:3306 --name sepa-rate-mysql -e MYSQL_ROOT_PASSWORD=password -d mysql:8
3. Connecting to it using eg: IntelliJ:  
    Host: 0.0.0.0
    Port: 3306
    User: root
    Password: see step 2
4. Run initial.sql
    
#Backend
Run SepaRateApplication to start backend. This will also run all flyway scripts, thus filling up your db.
 