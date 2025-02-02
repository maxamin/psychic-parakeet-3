using System.Collections.Generic;
using System.Linq;
using Owasp.Net.Models;
using OwaspDemo.Data;

namespace Owasp.Net.Services
{
    public interface IUserSessionStore
    {
        void Add(UserSession session);
        IEnumerable<UserSession> GetAllSessions();
    }

    public class UserSessionStore : IUserSessionStore
    {
        private readonly ApplicationDbContext _context;

        public UserSessionStore(ApplicationDbContext context)
        {
            _context = context;
        }

        public void Add(UserSession session)
        {
            _context.UserSessions.Add(session);
            _context.SaveChanges();
        }

        public IEnumerable<UserSession> GetAllSessions()
        {
            return _context.UserSessions.ToList();
        }
    }
}
