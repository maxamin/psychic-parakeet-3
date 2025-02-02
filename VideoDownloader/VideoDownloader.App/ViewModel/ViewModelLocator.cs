using Autofac;
using Microsoft.Practices.ServiceLocation;
using VideoDownloader.App.BL;
using VideoDownloader.App.Contract;

namespace VideoDownloader.App.ViewModel
{
    /// <summary>
    /// This class contains static references to all the view models in the
    /// application and provides an entry point for the bindings.
    /// </summary>
    public class ViewModelLocator
    {
        /// <summary>
        /// Initializes a new instance of the ViewModelLocator class.
        /// </summary>
        public ViewModelLocator()
        {
			var builder = new ContainerBuilder();

			builder.RegisterType<PluralsightCourseService>().As<ICourseService>().SingleInstance();
            builder.RegisterType<PluralsightLoginService>().As<ILoginService>().SingleInstance();
            builder.RegisterType<FileConfigProfider>().As<IConfigProvider>().SingleInstance();
			builder.RegisterType<PluralsightMainViewModel>().AsSelf();
			builder.RegisterType<LoginViewModel>().AsSelf();
			builder.RegisterType<SettingsViewModel>().AsSelf();
			var container = builder.Build();

			ServiceLocator.SetLocatorProvider(() => new AutofacServiceLocator(container));
			
		}

        public PluralsightMainViewModel MainVm => ServiceLocator.Current.GetInstance<PluralsightMainViewModel>();

	    public LoginViewModel LoginVm => ServiceLocator.Current.GetInstance<LoginViewModel>();

	    public SettingsViewModel SettingsVm => ServiceLocator.Current.GetInstance<SettingsViewModel>();

	    public static void Cleanup()
        {
            // TODO Clear the ViewModels
        }
    }
}