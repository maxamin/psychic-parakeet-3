using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;
using VideoDownloader.App.ViewModel;

namespace VideoDownloader.App
{
	/// <summary>
	/// Interaction logic for SettingsWindow.xaml
	/// </summary>
	public partial class SettingsWindow : Window
	{
		public SettingsWindow()
		{
			InitializeComponent();
			Closing += (s, e) => ViewModelLocator.Cleanup();
			Messenger.Default.Register<NotificationMessage>(this, (message) =>
			{
				switch (message.Notification)
				{
					case "CloseSettingsWindow":
						Close();
						break;
				}
			});
		}
	}
}

// implement System.Windows.Forms.IWin32Window