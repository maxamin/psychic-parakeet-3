using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;

namespace OwaspDemo.Controllers
{
    public class CreditController : Controller
    {
        public IActionResult Manage()
        {
            return View();
        }
    }
}
