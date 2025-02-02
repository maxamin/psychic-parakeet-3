using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Identity;
using Microsoft.Extensions.DependencyInjection;
using OwaspDemo.Data;
using OwaspDemo.Models;

namespace OwaspDemo.Extensions
{
    public static class DeveloperExtensions
    {
        public static void SeedDatabase(this IApplicationBuilder app)
        {
            var scopeFactory = app.ApplicationServices.GetRequiredService<IServiceScopeFactory>();
            using (var serviceScope = scopeFactory.CreateScope())
            {
                var userManager = serviceScope.ServiceProvider.GetService<UserManager<ApplicationUser>>();
                var context = serviceScope.ServiceProvider.GetService<ApplicationDbContext>();

                foreach (var user in GetUsers())
                {
                    var result = userManager.CreateAsync(user, "Password11").Result;
                }

                foreach (var post in GetDefaultBlogPosts())
                {
                    context.BlogPosts.Add(post);
                }

                context.SaveChanges();
            }
        }

        private static IEnumerable<BlogPost> GetDefaultBlogPosts()
        {
            yield return new BlogPost
            {
                Title = "Hello World!",
                Author = "admin@owasp.ninja",
                Content = "Hello World!",
                PublishedDate = DateTime.Now
            };
            yield return new BlogPost
            {
                Title = "Intro to HTML",
                Author = "homer@owasp.ninja",
                Content = "Hi guys, today I will write a quick post to teach you guys HTML.<br /><br /><strong>This is bolded text</strong><i>This is italics</i><br /><br /><a href=\"https://www.google.co.nz\">This points to google</a><br /><br />",
                PublishedDate = DateTime.Now
            };
            yield return new BlogPost
            {
                Title = "I will not hack this site",
                Author = "bart@owasp.ninja",
                Content = "<p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p><p>I will not hack this site</p>",
                PublishedDate = DateTime.Now
            };
            yield return new BlogPost
            {
                Title = "Test",
                Author = "test@owasp.ninja",
                Content = "On the other hand, we denounce with righteous indignation and dislike men who are so beguiled and demoralized by the charms of pleasure of the moment, so blinded by desire, that they cannot foresee the pain and trouble that are bound to ensue; and equal blame belongs to those who fail in their duty through weakness of will, which is the same as saying through shrinking from toil and pain. These cases are perfectly simple and easy to distinguish. In a free hour, when our power of choice is untrammelled and when nothing prevents our being able to do what we like best, every pleasure is to be welcomed and every pain avoided. But in certain circumstances and owing to the claims of duty or the obligations of business it will frequently occur that pleasures have to be repudiated and annoyances accepted. The wise man therefore always holds in these matters to this principle of selection: he rejects pleasures to secure other greater pleasures, or else he endures pains to avoid worse pains.",
                PublishedDate = DateTime.Now
            };
        }

        private static IEnumerable<ApplicationUser> GetUsers()
        {
            yield return new ApplicationUser
            {
                UserName = "admin@owasp.ninja",
                Email = "admin@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "homer@owasp.ninja",
                Email = "homer@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "marge@owasp.ninja",
                Email = "marge@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "bart@owasp.ninja",
                Email = "bart@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "lisa@owasp.ninja",
                Email = "lisa@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "yoda@owasp.ninja",
                Email = "yoda@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "peter@owasp.ninja",
                Email = "peter@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "jack.sparrow@owasp.ninja",
                Email = "jack.sparrow@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "jack.sparrow@owasp.ninja",
                Email = "jack.sparrow@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "test@owasp.ninja",
                Email = "test@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "jack.hugh@owasp.ninja",
                Email = "jack.hugh@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "legolas@owasp.ninja",
                Email = "legolas@owasp.ninja"
            };
            yield return new ApplicationUser
            {
                UserName = "bruce.wayne@owasp.ninja",
                Email = "bruce.wayne@owasp.ninja"
            };
        }
    }
}
