using System.Collections.Generic;
using OwaspDemo.Models;

namespace OwaspDemo.Dtos
{
    public class SearchDto
    {
        public string Keyword { get; set; }
        public List<string> Results { get; set; }
    }
}
