using System.ComponentModel;

namespace VideoDownloader.App
{
	public enum AcceptHeader
	{
		[Description("application/json, text/plain, */*")]
		JsonTextPlain,
		[Description("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")]
		HtmlXml,
        [Description("*/*")]
        All
	}

	public enum ContentType
	{
		[Description("application/json;charset=utf-8")]
		AppJsonUtf8,
		[Description("application/x-www-form-urlencoded")]
		AppXWwwFormUrlencode,
        [Description("video/mp4")]
        Video

	}

	public enum LoginStatus
	{
		LoggedIn,
		Failed
	}
}
