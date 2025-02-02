using System;
using System.Windows.Data;

namespace VideoDownloader.App.Converters
{
	class ProgressConverter : IValueConverter
	{
		#region IValueConverter Members
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			int progress = System.Convert.ToInt32(value);
			if (progress != -1)
			{
				return value;
			}
			else
			{
				return "undefined size";
			}
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
		#endregion
	}
}
