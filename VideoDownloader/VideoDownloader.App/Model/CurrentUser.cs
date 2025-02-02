namespace VideoDownloader.App.Model
{
    public class CurrentUser
    {
        public string Id { get; set; }

        public string FirstName { get; set; }

        public string LastName { get; set; }

        public string DisplayName { get; set; }

        public string Email { get; set; }

        public string UserName { get; set; }

        public UserAvatar Avatar { get; set; }

        public string CreatedAt { get; set; }

        public dynamic[] Roles { get; set; }

        public string[] UserFlags { get; set; }

        public string[] SliceSubscriptions { get; set; }

        public Subscription[] Subscriptions { get; set; }

        public dynamic Config { get; set; }
    }
}