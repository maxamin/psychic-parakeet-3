using System.Collections.Generic;
using System.Data.SQLite;
using System.Web.Http;
using TarpitCsharp.Utils;

namespace TarpitCsharp.Controllers
{
    public class LoginHandler: ApiController
    {
        // GET api/values
        [Microsoft.AspNetCore.Mvc.HttpGet]
        [Microsoft.AspNetCore.Mvc.Route("login")]
        public List<string> HandleGet([FromUri] LQuery query)
        {

            var theUser = query.userId;
            var thePassword = query.password;
            var role = query.role;

            if (!Login(theUser, thePassword))
            {
                RedirectToRoute("lognin", query);
            }

            return role == "customer" ? LoggedIn() : NotLoggedIn();
        }
        
        [Microsoft.AspNetCore.Mvc.HttpGet]
        [Microsoft.AspNetCore.Mvc.Route("app")]
        public List<string> LoggedIn()
        {
            return new List<string> {"Successfully logged in"};
        }
        
        public List<string> NotLoggedIn()
        {
            return new List<string> {"Not logged in"};
        }

        public bool Login(string user, string pass)
        {
            var sql = new SQLiteCommand("SELECT * FROM USER WHERE login = @login AND PASSWORD = @pass",
                DatabaseUtils._con);

            sql.Parameters.Add(new SQLiteParameter("@login", user));
            sql.Parameters.Add(new SQLiteParameter("@pass", user));
            return sql.ExecuteNonQuery() > 0;
        }
    }
    
    public class LQuery
    {
        public string userId { get; set; }
        public string password { get; set; }
        public string role { get; set; }
    }
}