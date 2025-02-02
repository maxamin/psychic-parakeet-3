using System;
using System.Collections.Generic;
using System.Data.SQLite;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Web.Http;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using TarpitCsharp.Utils;

namespace TarpitCsharp.Controllers
{
    public class VulnerabilitiesController : Controller
    {
        [Microsoft.AspNetCore.Mvc.HttpGet]
        [Microsoft.AspNetCore.Mvc.Route("vuln")]
        public IActionResult HandleGet([FromUri] VulnQuery query)
        {
            var ret = new List<string>();
            var ACCESS_KEY_ID = "AKIA2E0A8F3B244C9986";
            var SECRET_KEY = "7CE556A3BC234CC1FF9E8A5C324C0BB70AA21B6D";

            var login = query.Login;
            var password = query.Password;
            var keeponline = query.Keeponline != null;
           

            var sql = new SQLiteCommand("SELECT * FROM USER WHERE login = '" + login + "' AND PASSWORD = '" + password + "'",
                    DatabaseUtils._con);

            var reader = sql.ExecuteReader();

            if (reader.Read())
            {
                var user = new User(
                    reader["fname"].ToString(), 
                    reader["lname"].ToString(), 
                    reader["passportnum"].ToString(), 
                    reader["address1"].ToString(), 
                    reader["address2"].ToString(),
                    reader["zipcode"].ToString());
                
                var option = new CookieOptions();
                option.MaxAge = TimeSpan.Parse("864000");
                option.Path = "/";
                Response.Cookies.Append("login", login, option);

                Logger.Info($"User {user} successfully logged in");


                var provider = new DESCryptoServiceProvider();                
                provider.GenerateKey();
                provider.GenerateIV();

                var creditinfo = reader["creditinfo"].ToString();
                EncryptString(provider, creditinfo);

                var msg = $"User {user} credit info is {creditinfo}";
                ret.Append(msg);
                Logger.Info(msg);
                
                return LocalRedirect("fwd");
            }

            Logger.Info($"User {login} failed to sign in");
            return new JsonResult($"User {login} failed to sign in");

        }
        
        // GET api/values
        [Microsoft.AspNetCore.Mvc.HttpGet]
        [Microsoft.AspNetCore.Mvc.Route("fwd")]
        public IActionResult HandleFwd([FromUri] VulnQuery query)
        {
            return new JsonResult("successfully logged in");
        }
        
        public static byte[] EncryptString(SymmetricAlgorithm symAlg, string inString)
        {
            byte[] inBlock = UnicodeEncoding.Unicode.GetBytes(inString);
            ICryptoTransform xfrm = symAlg.CreateEncryptor();
            byte[] outBlock = xfrm.TransformFinalBlock(inBlock, 0, inBlock.Length);

            return outBlock;
        }

        public static string DecryptBytes(SymmetricAlgorithm symAlg, byte[] inBytes)
        {
            ICryptoTransform xfrm = symAlg.CreateDecryptor();
            byte[] outBlock = xfrm.TransformFinalBlock(inBytes, 0, inBytes.Length);

            return UnicodeEncoding.Unicode.GetString(outBlock);
        }
    }

    public class VulnQuery
    {
        public string Login { get; set; }
        public string Password { get; set; }
        public string Keeponline { get; set; }
    }
    
    public class User
    {
        public User(string fname, string lname, string passportnum, string address1, string address2, string zipcode)
        {
            Fname = fname;
            Lname = lname;
            Passportnum = passportnum;
            Address1 = address1;
            Address2 = address2;
            Zipcode = zipcode;
        }

        public string Fname { get; set; }
        public string Lname { get; set; }
        public string Passportnum { get; set; }
        public string Address1 { get; set; }
        public string Address2 { get; set; }
        public string Zipcode { get; set; }
    }
}