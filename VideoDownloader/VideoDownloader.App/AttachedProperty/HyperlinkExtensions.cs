using System;
using System.Diagnostics;
using System.Windows;
using System.Windows.Documents;

namespace VideoDownloader.App.AttachedProperty
{
    public class HyperlinkExtensions
    {
        public static string GetUrlFormat(DependencyObject obj)
        {
            return (string)obj.GetValue(UrlFormatProperty);
        }

        public static void SetUrlFormat(DependencyObject obj, string value)
        {
            obj.SetValue(UrlFormatProperty, value);
        }

        public static readonly DependencyProperty UrlFormatProperty =
            DependencyProperty.RegisterAttached("UrlFormat", typeof(string), typeof(HyperlinkExtensions), new UIPropertyMetadata(string.Empty, OnUrlFormatChanged));

        private static void OnUrlFormatChanged(object sender, DependencyPropertyChangedEventArgs args)
        {
            var hyperlink = sender as Hyperlink;
            if (!string.IsNullOrEmpty((string) args.NewValue))
            {
                hyperlink.NavigateUri = new Uri(string.Format((string) args.NewValue, hyperlink.NavigateUri));

                hyperlink.RequestNavigate -= Hyperlink_RequestNavigate;
                hyperlink.RequestNavigate += Hyperlink_RequestNavigate;
            }
            else
            {
                hyperlink.RequestNavigate -= Hyperlink_RequestNavigate;
            }
        }


        private static void Hyperlink_RequestNavigate(object sender, System.Windows.Navigation.RequestNavigateEventArgs e)
        {
            Process.Start(new ProcessStartInfo(e.Uri.AbsoluteUri));
        }
    }
}
