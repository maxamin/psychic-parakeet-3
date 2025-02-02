using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using Owasp.Net.Models;
using Owasp.Net.Services;

namespace Owasp.Net.Controllers
{
    public class DodgyController : Controller
    {
        private readonly IUserSessionStore _sessionStore;

        public DodgyController(IUserSessionStore sessionStore)
        {
            _sessionStore = sessionStore;
        }

        public void Cookie(string cookie)
        {
            _sessionStore.Add(new UserSession
            {
                Cookie = cookie
            });
        }

        [HttpGet]
        [Route("api/usersessions")]
        public IEnumerable<UserSession> GetUserSessions()
        {
            return _sessionStore.GetAllSessions();
        }
    }
}