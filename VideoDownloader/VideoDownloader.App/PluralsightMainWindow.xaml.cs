using GalaSoft.MvvmLight.Messaging;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Remoting.Channels;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;
using VideoDownloader.App.BL.Messages;
using VideoDownloader.App.ViewModel;

namespace VideoDownloader.App
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();
            var vm = (PluralsightMainViewModel)DataContext;
            Loaded += (sender, args) =>
            {
                vm.Messenger.Register<OpenSettingsMessage>(this, OpenSettingsWindow);
                vm.Messenger.Register<ExceptionThrownMessage>(this, ShowExceptionThrownMessageBox);
            };

            Unloaded += (sender, args) =>
            {
                vm.Messenger.Register<OpenSettingsMessage>(this, message => { });
                vm.Messenger.Register<ExceptionThrownMessage>(this, message => { });
            };
        }

        private void ShowExceptionThrownMessageBox(ExceptionThrownMessage message)
        {
            MessageBox.Show(message.Text, "Video downloader", MessageBoxButton.OK);
        }

        private void OpenSettingsWindow(OpenSettingsMessage message)
        {
            var settingsWindow = new SettingsWindow();
            settingsWindow.Show();
        }
    }
}
