using System;
using System.Collections.Generic;
using System.Linq;
using OwaspDemo.Data;
using OwaspDemo.Models;

namespace OwaspDemo.Services
{
    public interface IUserSearchService
    {
        IEnumerable<string> SearchUsers(string keyword);
    }

    public class UserSearchService : IUserSearchService
    {
        private readonly ApplicationDbContext _context;

        public UserSearchService(ApplicationDbContext context)
        {
            _context = context;
        }

        public IEnumerable<string> SearchUsers(string keyword)
        {
            if (String.IsNullOrEmpty(keyword)) return new List<string>();
            return _context.Users.Where(u => u.Email.Contains(keyword) || u.UserName.Contains(keyword)).Select(u => u.Email).ToList();
        }
    }
}
