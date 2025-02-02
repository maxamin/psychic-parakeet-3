using System;
using System.ComponentModel.DataAnnotations;

namespace OwaspDemo.Models
{
    public class BlogPost
    {
        public Guid Id { get; set; }
        public string Author { get; set; }
        [MinLength(3)]
        public string Title { get; set; }

        public string Content { get; set; }
        public DateTime PublishedDate { get; set; }
    }
}
