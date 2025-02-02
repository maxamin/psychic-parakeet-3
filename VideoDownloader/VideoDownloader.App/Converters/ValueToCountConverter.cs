using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Data;
using VideoDownloader.App.Model;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(string), typeof(int))]
	public class ValueToCountConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
		    var result = value as string;
		    return int.Parse(result);
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
