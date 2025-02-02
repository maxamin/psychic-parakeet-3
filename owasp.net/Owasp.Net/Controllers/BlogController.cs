using System;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using OwaspDemo.Models;
using OwaspDemo.Services;

namespace OwaspDemo.Controllers
{
    [Authorize]
    public class BlogController : Controller
    {
        private readonly IBloggingService _bloggingService;

        public BlogController(IBloggingService bloggingService)
        {
            _bloggingService = bloggingService;
        }

        public IActionResult GetPost(Guid id)
        {
            var post = _bloggingService.GetBlogPost(id);
            return View(post);
        }

        public IActionResult Posts()
        {
            var email = HttpContext.User.Identity.Name;
            var posts = _bloggingService.GetPostsByUser(email);
            return View(posts);
        }

        public IActionResult Write(BlogPost post)
        {
            return View();
        }

        public IActionResult WritePost(BlogPost post)
        {
            if (ModelState.IsValid)
            {
                // Weak sanitisation code
                //var content = post.Content.Replace("<script>", "&lt;script&gt;");
                //content.Replace("</script>", "&lt;/script&gt;");
                //post.Content = content;

                post.Author = HttpContext.User.Identity.Name;
                _bloggingService.AddPost(post);

                return RedirectToAction("Posts");
            }

            return RedirectToAction("Write");
        }
    }
}
