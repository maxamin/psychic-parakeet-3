using System;

namespace Owasp.Net.Models
{
    public class UserSession
    {
        public Guid Id { get; set; }
        public string Cookie { get; set; }
    }
}
