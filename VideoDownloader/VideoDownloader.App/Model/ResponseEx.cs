using System.Net.Http;

namespace VideoDownloader.App.Model
{
	public class ResponseEx
	{
		public string Content { get; set; }

		public int ContentLength { get; set; }

        public string Cookies { get; set; }

        public string RedirectUrl { get; set; }

	    public HttpResponseMessage ResponseMessage { get; set; }
	}
}
