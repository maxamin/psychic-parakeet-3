using System;
using System.Collections.Generic;
using System.Linq;
using OwaspDemo.Data;
using OwaspDemo.Models;

namespace OwaspDemo.Services
{
    public interface IBloggingService
    {
        BlogPost GetBlogPost(Guid id);
        IEnumerable<BlogPost> GetAllPosts();
        IEnumerable<BlogPost> GetPostsByUser(string email);

        void AddPost(BlogPost post);
    }

    public class BloggingService : IBloggingService
    {
        private readonly ApplicationDbContext _context;

        public BloggingService(ApplicationDbContext context)
        {
            _context = context;
        }

        public BlogPost GetBlogPost(Guid id)
        {
            return _context.BlogPosts.Single(u => u.Id == id);
        }

        public IEnumerable<BlogPost> GetAllPosts()
        {
            return _context.BlogPosts.OrderByDescending(u => u.PublishedDate).ToList();
        }

        public IEnumerable<BlogPost> GetPostsByUser(string email)
        {
            return _context.BlogPosts.Where(u => u.Author.ToLower().Equals(email.ToLower())).ToList();
        }

        public void AddPost(BlogPost post)
        {
            post.PublishedDate = DateTime.Now;
            _context.BlogPosts.Add(post);
            _context.SaveChanges();
        }
    }
}
