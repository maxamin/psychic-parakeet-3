using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using VideoDownloader.App.Contract;

namespace VideoDownloader.App.BL
{
	class FileConfigProfider: IConfigProvider
	{
        #region Constants

	    readonly string[] _defaultUserAgents = {"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0",
                    "Mozilla/5.0 (iPhone; CPU iPhone OS 9_0 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13A342 Safari/601.1",
                    "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36",
                    "Mozilla/5.0 (compatible; MSIE 10.0; Windows Phone 8.0; Trident/6.0; IEMobile/10.0; ARM; Touch; Microsoft; Lumia 640 XL)",
                    "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko"};

        #endregion
        #region Fields

        private string _path => "videodownloader.settings";
	    private readonly List<string> _userAgents;

        #endregion

        #region Properties

        public int MinTimeout { get; set; } = 60;

		public int MaxTimeout { get; set; } = 120;

		public string UserAgent { get; }

		public string DownloadsPath { get; set; } = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);

		#endregion

		public FileConfigProfider()
		{
			if (File.Exists(_path))
			{
				var readSetting = File.ReadAllText(_path);
				Dictionary<string, object> values = JsonConvert.DeserializeObject<Dictionary<string, object>>(readSetting);
				MinTimeout = Convert.ToInt32(values["mintimeout"]);
				MaxTimeout = Convert.ToInt32(values["maxtimeout"]);

                _userAgents = JsonConvert.DeserializeObject<List<string>>(Convert.ToString(values["userAgents"]));
			}
			else
			{
                _userAgents = new List<string>(_defaultUserAgents);
				Save();
			}

			if (UserAgent == null)
			{
			    UserAgent = GetRandomUserAgent();
			}
		}

		public void Save()
		{
			try
			{
				var settings = JsonConvert.SerializeObject(new
				{
				    mintimeout = MinTimeout,
                    maxtimeout = MaxTimeout,
                    downloadspath = DownloadsPath,
                    userAgents = _userAgents
				}, Formatting.Indented);

				using (TextWriter tw = new StreamWriter(_path))
				{
					tw.Write(settings);
					tw.Close();
				}
			}
			catch (JsonException /*exc*/)
			{
			}
		}

	    private string GetRandomUserAgent()
		{
			var rnd = new Random(Guid.NewGuid().GetHashCode());
			return _userAgents.ElementAt(rnd.Next(0, _userAgents.Count - 1));
		}
	}
}
