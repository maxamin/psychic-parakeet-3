using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Mvc;
using OwaspDemo.Dtos;
using OwaspDemo.Models;
using OwaspDemo.Services;

namespace OwaspDemo.Controllers
{
    public class HomeController : Controller
    {
        private readonly IBloggingService _bloggingService;
        private readonly IUserSearchService _userSearchService;

        public HomeController(IUserSearchService userSearchService, IBloggingService bloggingService)
        {
            _bloggingService = bloggingService;
            _userSearchService = userSearchService;
        }

        public IActionResult Index()
        {
            var topPosts = _bloggingService.GetAllPosts().TakeLast(10);
            return View(topPosts);
        }

        public IActionResult About()
        {
            ViewData["Message"] = "Your application description page.";

            return View();
        }

        public IActionResult Contact()
        {
            ViewData["Message"] = "Your contact page.";

            return View();
        }

        public IActionResult Search(SearchDto search)
        {
            search.Results = _userSearchService.SearchUsers(search.Keyword).ToList();
            return View(search);
        }

        public IActionResult Error()
        {
            return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
        }
    }
}
