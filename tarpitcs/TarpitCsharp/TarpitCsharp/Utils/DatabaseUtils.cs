using System;
using System.Data.SQLite;
using System.IO;

namespace TarpitCsharp.Utils
{
    public class DatabaseUtils
    {
        private static string _db = "dummy.db";
        
        public static SQLiteConnection _con;
     
        public static string GetConnectionString()
        {
            return "DataSource=" +_db + ";" ;
        }

        public static void init()
        {
            if(File.Exists(_db))
                File.Delete(_db);
         
            Logger.Info("Init database");
            Console.WriteLine("init");
            SQLiteConnection.CreateFile(_db);
            
            _con = new SQLiteConnection(GetConnectionString());
            _con.Open();

            try
            {
              var sqlUsers = @"CREATE TABLE users(
                  id INT, 
                  fname VARCHAR(20), 
                  lname VARCHAR(20),
                  passportnum VARCHAR(20),
                  address1 VARCHAR(20),
                  address2 VARCHAR(20),
                  zipcode VARCHAR(20),
                  login VARCHAR(20),
                  password VARCHAR(20),
                  creditinfo VARCHAR(20))";

              var sqlOrders = @"CREATE TABLE orders(
                  orderId INT,
                  custId INT, 
                  orderDate VARCHAR(20), 
                  orderStatus VARCHAR(20),
                  shipDate varchar(20),
                  street VARCHAR(20),
                  city VARCHAR(20),
                  state VARCHAR(20),
                  zipCode VARCHAR(20))";

              var insertAlice =
                  @"INSERT INTO users(id, fname, lname, passportnum, address1, address2, zipcode, login, password) VALUES (
                  1, 
                  ""Alice"", 
                  ""Test"",
                  ""1123"",
                  ""Test Avenue"",
                  ""CA"",
                  ""alice"",
                  ""alicepw"",
                  ""1323912491293"")";
    
                var insertOrder = @"INSERT INTO orders(
                  orderId, 
                  custId, 
                  orderDate, 
                  orderStatus, 
                  shipDate, 
                  street, 
                  state, 
                  zipCode) 
                  VALUES (
                  1, 
                  1, 
                  ""2002/01/31"",
                  ""completed"",
                  ""2002/01/29"",
                  ""Downing Street"",
                  ""CA"",
                  ""3123"")";
                
                new SQLiteCommand(sqlUsers, _con).ExecuteNonQuery();
                new SQLiteCommand(sqlOrders, _con).ExecuteNonQuery();
                new SQLiteCommand(insertAlice, _con).ExecuteNonQuery();
                new SQLiteCommand(insertOrder, _con).ExecuteNonQuery();
            
            } catch (SQLiteException e){
                Logger.Error(e.Message);
                Environment.Exit(-1);
            }
        }
    }
}