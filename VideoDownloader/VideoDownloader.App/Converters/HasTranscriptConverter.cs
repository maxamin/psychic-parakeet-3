using System;
using System.Windows.Data;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(bool), typeof(string))]
	public class HasTranscriptConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			if (targetType != typeof(string))
			{
				throw new InvalidOperationException("The target must be string");
			}

			return value != null && (bool)value ? Properties.Resources.Yes : Properties.Resources.No;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
