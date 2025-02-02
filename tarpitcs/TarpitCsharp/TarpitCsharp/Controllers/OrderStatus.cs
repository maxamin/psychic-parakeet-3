using System;
using System.Data.SQLite;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Rewrite.Internal.UrlActions;
using Microsoft.EntityFrameworkCore.ChangeTracking.Internal;
using Microsoft.VisualStudio.TestPlatform.Common.DataCollection;
using TarpitCsharp.Utils;

namespace TarpitCsharp.Controllers
{
    public class OrderStatus : Controller
    {

        [Route("order/")]
        public ActionResult Index()
        {

            var orderId = Request.Query["orderId"].ToString();
            
            var sql = new SQLiteCommand("SELECT * FROM orders WHERE orderid = @orderid",
                DatabaseUtils._con);
            
            Logger.Info($"Received orderId {orderId}");

            sql.Parameters.Add(new SQLiteParameter("@orderid", orderId));

            var reader = sql.ExecuteReader();

            if (reader.Read())
            {
                var order = new Order(Int32.Parse(reader["custId"].ToString()),
                    Int32.Parse(reader["orderId"].ToString()),
                    reader["orderDate"].ToString(),
                    reader["orderStatus"].ToString(),
                    reader["shipDate"].ToString(),
                    reader["street"].ToString(),
                    reader["city"].ToString(),
                    reader["state"].ToString(),
                    reader["zipCode"].ToString());

                var option = new CookieOptions();
                option.MaxAge = TimeSpan.Parse("864000");
                option.Path = "/";
                Response.Cookies.Append("order", order.orderId.ToString(), option);
                
                Logger.Info($"Order details are {order}");
                return new JsonResult(order.ToString());

            }

            return new JsonResult($"Order {orderId} does not exist");
        }

        public class Order
        {
            public Order(int custId, int orderId, string orderDate, string orderStatus, string shipDate, string street,
                string city, string state, string zipCode)
            {
                this.custId = custId;
                this.orderId = orderId;
                this.orderDate = orderDate;
                this.orderStatus = orderStatus;
                this.shipDate = shipDate;
                this.street = street;
                this.city = city;
                this.state = state;
                this.zipCode = zipCode;
            }

            public override string ToString()
            {
                return "custId:" + custId + ", orderId:" + orderId + ",orderDate:" + orderDate + ",shipDate:" +
                       shipDate + ",street:" + street + ",city:" + city + ",state:" + state + ",zipCode:" + zipCode;
            }

            public int custId { get; set; }
            public int orderId { get; set; }
            public string orderDate { get; set; }
            public string orderStatus { get; set; }
            public string shipDate { get; set; }
            public string street { get; set; }
            public string city { get; set; }
            public string state { get; set; }
            public string zipCode { get; set; }
        }
    }
}