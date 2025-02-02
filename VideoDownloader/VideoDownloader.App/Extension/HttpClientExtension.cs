using System;
using System.IO;
using System.Net.Http;
using System.Threading.Tasks;

namespace VideoDownloader.App.Extension
{
	public static class HttpClientExtension
	{
		public static async Task ReadAsFileAsync(this HttpContent content, string filename, bool overwrite)
		{
			var pathname = Path.GetFullPath(filename);
			if (!overwrite && File.Exists(filename))
			{
				throw new InvalidOperationException($"File {pathname} already exists.");
			}

			using (var fileStream = new FileStream(pathname, FileMode.Create, FileAccess.Write, FileShare.None))
			{
				await content.CopyToAsync(fileStream);
				fileStream.Close();
			}
		}
	}
}