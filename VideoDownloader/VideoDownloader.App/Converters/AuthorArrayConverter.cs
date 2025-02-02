using System;
using System.Linq;
using System.Windows.Data;
using VideoDownloader.App.Model;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(Author[]), typeof(string))]
	public class AuthorArrayConverter : IValueConverter
	{
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			if (targetType != typeof(string))
				throw new InvalidOperationException("The target must be a String");

		    string result = string.Empty;
		    var authors = (Author[]) value;
		    if (authors != null)
		    {
		        result = authors.Aggregate(result, (current, author) => current + $"{author.DisplayName}");
		    }
		    return result;
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			throw new NotImplementedException();
		}
	}
}
