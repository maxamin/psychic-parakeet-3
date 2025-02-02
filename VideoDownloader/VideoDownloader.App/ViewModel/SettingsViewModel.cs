using GalaSoft.MvvmLight;
using GalaSoft.MvvmLight.CommandWpf;
using GalaSoft.MvvmLight.Messaging;
using System;
using System.Windows.Forms;
using VideoDownloader.App.Contract;

namespace VideoDownloader.App.ViewModel
{
	public class SettingsViewModel : ViewModelBase
	{
		#region Fields

	    int _minTimeout;
		int _maxTimeout;
		string _downloadsPath;

		#endregion

		#region Properties

		public RelayCommand OpenSelectFolderDialogCommand { get; set; }

		public RelayCommand SaveSettingsCommand { get; set; }

		public RelayCommand CancelCommand { get; set; }

	    private IConfigProvider _configProvider;

        public int MinTimeout
		{
			get { return _minTimeout; }
			set
			{
				Set(() => MinTimeout, ref _minTimeout, value);
			}
		}

		public int MaxTimeout
		{
			get { return _maxTimeout; }
			set
			{
				Set(() => MaxTimeout, ref _maxTimeout, value);
			}
		}

		public string DownloadsPath
		{
			get { return _downloadsPath; }
			set
			{
				Set(() => DownloadsPath, ref _downloadsPath, value);
			}
		}

		#endregion

		#region Constructor

		public SettingsViewModel(IConfigProvider configProvider)
		{
            _configProvider = configProvider;

			MinTimeout = _configProvider.MinTimeout;
			MaxTimeout = _configProvider.MaxTimeout;
			DownloadsPath = _configProvider.DownloadsPath;

			OpenSelectFolderDialogCommand = new RelayCommand(() =>
			{
				var dialog = new FolderBrowserDialog();
				var selectedPath = dialog.ShowDialog();
				if (selectedPath == DialogResult.OK)
				{
					DownloadsPath = dialog.SelectedPath;
				}
			});

			SaveSettingsCommand = new RelayCommand(() =>
			{
			    _configProvider.MinTimeout = MinTimeout;
			    _configProvider.MaxTimeout = MaxTimeout;
			    _configProvider.DownloadsPath = DownloadsPath;

			    _configProvider.Save();
				try
				{
					Messenger.Default.Send(new NotificationMessage("CloseSettingsWindow"));
				}
			    catch (Exception /*exc*/)
			    {
			        // ignored
			    }
			});

			CancelCommand = new RelayCommand(() =>
			{
				Messenger.Default.Send(new NotificationMessage("CloseSettingsWindow"));
			});
		}
	}
	#endregion
}
