using System.Threading.Tasks;

namespace VideoDownloader.App.Contract
{
    public interface ILoginService
    {
		Task<LoginResult> LoginAsync(string userName, string password);

        string LoginResultJson { get; set; }

    }
}