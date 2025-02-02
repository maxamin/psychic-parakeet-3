using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.CommandWpf;
using GalaSoft.MvvmLight.Messaging;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Threading.Tasks;
using VideoDownloader.App.Contract;

namespace VideoDownloader.App.ViewModel
{
	public class LoginViewModel: ViewModelBase, IDataErrorInfo
	{
		#region Fields

        private readonly ICourseService _courseService;
        private readonly ILoginService _loginService;
	    private string _userName;

        private bool _loginButtonEnabled;
		private bool _loggingInAnimationVisible;
		private bool _firstUserNameCheck = true;
		private string _currentOperation = string.Empty;
	    private bool _useCachedListOfProducts;

	    #endregion

		#region Constructors

		public LoginViewModel(ILoginService loginService, ICourseService сourseService)
		{
			_courseService = сourseService;
		    _loginService = loginService;
			LoginButtonEnabled = true;
			LoginInProgress = false;
			LoginCommand = new RelayCommand<object>(obj => OnLogin(obj), Login_CanExecute);
			CloseCommand = new RelayCommand<ICloseable>(CloseWindow);
		}
		#endregion

		#region Properties

		public RelayCommand<object> LoginCommand { get; private set; }

		public RelayCommand<ICloseable> CloseCommand { get; private set; }

	    public bool UseCachedListOfProducts
	    {
	        get { return _useCachedListOfProducts; }
	        set { Set(() => UseCachedListOfProducts, ref _useCachedListOfProducts, value); }
	    }

		public string UserName
		{
			get
			{
                return _userName;
			}

			set
			{
                Set(() => UserName, ref _userName, value);
                LoginButtonEnabled = ValidateUserName() && !LoginInProgress;
			}
		}

	    private string Password { get; set; }
	    
		public string CurrentOperation
		{
			get { return _currentOperation; }
			set
			{
				Set(() => CurrentOperation, ref _currentOperation, value);
			}
		}

		public bool LoginButtonEnabled
		{
			get { return _loginButtonEnabled; }
			set
			{
				Set(() => LoginButtonEnabled, ref _loginButtonEnabled, value);
			}
		}

		public bool ShowAnimation
		{
			get { return _loggingInAnimationVisible; }
			set
			{
				Set(() => ShowAnimation, ref _loggingInAnimationVisible, value);
			}
		}

	    private bool LoginInProgress { get; set; }

		#endregion

		#region commands

		#endregion

		#region command executors

		private async Task OnLogin(object passwordControl)
		{
            LoginButtonEnabled = false;
            Password = GetPassword(passwordControl);
            CurrentOperation = Properties.Resources.TryingToLogin;
            LoginInProgress = true;

            LoginResult loginResult = await _loginService.LoginAsync(UserName, Password);

			if (loginResult.Status == LoginStatus.LoggedIn)
			{
				CurrentOperation = UseCachedListOfProducts ? Properties.Resources.ReadingCachedProducts : Properties.Resources.DownloadingListOfProducts;
			    _courseService.Cookies = loginResult.Cookies;

                bool received = UseCachedListOfProducts ? await _courseService.ProcessCachedProductsAsync() : await _courseService.ProcessNoncachedProductsJsonAsync();
				if (received)
				{
				    File.WriteAllText(Properties.Settings.Default.FileNameForJsonOfCourses, _courseService.CachedProductsJson);
                    Messenger.Default.Send(new NotificationMessage("CloseWindow"));
				}
				else
				{
					CurrentOperation = Properties.Resources.UnableToReceiveProducts;
					LoginInProgress = false;
					LoginButtonEnabled = true;
				}
			}
			else
			{
				CurrentOperation = Properties.Resources.LoginFailed;
				LoginInProgress = false;
				LoginButtonEnabled = true;
			}
		}

	    private static string GetPassword(object passwordControl)
	    {
	        System.Windows.Controls.PasswordBox pwBoxControl = passwordControl as System.Windows.Controls.PasswordBox;
	        Debug.Assert(pwBoxControl != null, "pwBoxControl != null");
	        var password = pwBoxControl.Password;
	        return password;
	    }

	    private void CloseWindow(ICloseable window)
		{
			window?.Close();
		}

		private bool Login_CanExecute(object o)
		{
			return LoginButtonEnabled && ValidateUserName();
		}

	    #endregion

		#region Auxiliary methods

	    private bool ValidateUserName() => !string.IsNullOrEmpty(UserName);

	    #endregion

		#region IDataError members

		public string this[string columnName]
		{
			get
			{
				string msg = null;
				switch (columnName)
				{
					case "UserName":
						{
							if (!_firstUserNameCheck && !ValidateUserName())
								msg = Properties.Resources.NameCannotBeEmpty;
							else
							{
								_firstUserNameCheck = false;
							}
							break;
						}
				}
				return msg;
			}
		}

		public string Error => string.Empty;

	    #endregion
	}
}
