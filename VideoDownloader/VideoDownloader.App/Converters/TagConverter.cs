using System;
using System.Windows.Data;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(string), typeof(string))]
	public class TagConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			if (targetType != typeof(string))
				throw new InvalidOperationException("The target must be a String");

			var tag = value as string;

			return tag?.Replace('-', ' ') ?? string.Empty;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
