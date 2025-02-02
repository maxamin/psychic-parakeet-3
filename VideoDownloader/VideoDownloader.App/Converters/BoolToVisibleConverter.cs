using System;
using System.Windows;
using System.Windows.Data;

namespace VideoDownloader.App.Converters
{
    [ValueConversion(typeof(bool), typeof(Visibility))]
    public class BoolToVisibleConverter : IValueConverter
	{
        #region IValueConverter Members

        public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			bool bValue = System.Convert.ToBoolean(value);
			return bValue ? Visibility.Visible : Visibility.Hidden;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			Visibility visibility = (Visibility)value;
			return visibility == Visibility.Visible;
		}
		
        #endregion
	}
}
