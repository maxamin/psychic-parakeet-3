using System;
using System.Windows.Data;
using System.Xml;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(string), typeof(TimeSpan))]
	public class IsoTimeToTimeSpanConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			if (targetType != typeof(string))
				throw new InvalidOperationException("The target must be a String");

			if (value != null)
			{
				TimeSpan timeSpan = XmlConvert.ToTimeSpan((string)value);

				return timeSpan.ToString(@"hh\:mm\:ss");
			}
			return new TimeSpan();
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
