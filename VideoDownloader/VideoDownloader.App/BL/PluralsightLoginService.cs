using System;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;
using VideoDownloader.App.Contract;
using VideoDownloader.App.Model;

namespace VideoDownloader.App.BL
{
    public class PluralsightLoginService:ILoginService
    {
        private string _cookies;
        private readonly IConfigProvider _configProvider;
        public string LoginResultJson { get; set; }

        public PluralsightLoginService(IConfigProvider configProvider)
        {
            _configProvider = configProvider;
        }

        public async Task<LoginResult> LoginAsync(string userName, string password)
        {
            var postData = BuildPostDataString(userName, password);
            
           
            var loginResponse = await TryLoginAsync(postData);
            var loginResult = GetLoginResult(loginResponse);
            LoginResultJson = loginResult.DataJson;
            return loginResult;
        }

        private async Task<ResponseEx> TryLoginAsync(string postData)
        {
            HttpMethod httpMethod = HttpMethod.Post;
            Uri urlToGo = new Uri(Properties.Settings.Default.FirstUrlForLogin);
            var httpHelper = new HttpHelper
            {
                AcceptHeader = AcceptHeader.HtmlXml,
                AcceptEncoding = string.Empty,
                ContentType = ContentType.AppXWwwFormUrlencode,
                Cookies = _cookies,
                Referrer = urlToGo,
                UserAgent = _configProvider.UserAgent
            };

            ResponseEx loginResponse;
            do
            {
                loginResponse = await httpHelper.SendRequest(httpMethod, urlToGo, postData, new CancellationToken());
                _cookies += $"{loginResponse.Cookies};";
                if (loginResponse.RedirectUrl != null)
                {
                    urlToGo = new Uri(loginResponse.RedirectUrl);
                }
                httpMethod = HttpMethod.Get;
                httpHelper.Cookies = _cookies;
            } while (loginResponse.ResponseMessage.StatusCode == HttpStatusCode.Redirect);
            return loginResponse;
        }

        private LoginResult GetLoginResult(ResponseEx loginResponse)
        {
            LoginResult loginResult = new LoginResult();
            string userData;
            if (IsLoggedIn(loginResponse.Content, out userData))
            {
                loginResult.Status = LoginStatus.LoggedIn;
                loginResult.DataJson = userData;
                loginResult.Cookies = _cookies;
            }
            else
            {
                loginResult.Status = LoginStatus.Failed;
            }
            return loginResult;
        }

        private string BuildPostDataString(string userName, string password)
        {
            return
                $"RedirectUrl=&Username={userName}" +
                $"&Password={password}" +
                "&ShowCaptcha=False" +
                "&ReCaptchaSiteKey=6LeVIgoTAAAAAIhx_TOwDWIXecbvzcWyjQDbXsaV";
        }

        bool IsLoggedIn(string content, out string loggedInUserData)
        {
            bool loggedIn = false;
            loggedInUserData = string.Empty;

            if (!string.IsNullOrEmpty(content))
            {
                var loginDoneRegExp = new Regex("{\"currentUser\":{.+\\:.+}");
                var loginDoneMatch = loginDoneRegExp.Match(content);
                if (loginDoneMatch.Success)
                {
                    loggedInUserData = loginDoneMatch.Groups[0].Value;
                    loggedIn = true;
                }
            }
            return loggedIn;
        }
    }
}