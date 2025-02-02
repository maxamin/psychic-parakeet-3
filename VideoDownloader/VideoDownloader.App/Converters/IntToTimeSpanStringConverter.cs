using System;
using System.Windows.Data;

namespace VideoDownloader.App.Converters
{
	[ValueConversion(typeof(int), typeof(string))]
	public class IntToTimeSpanStringConverter: IValueConverter
	{
		#region Constructors

		#endregion

		#region Properties
		public bool Collapse { get; set; }

		#endregion

		#region IValueConverter Members
		public object Convert(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			var val = System.Convert.ToInt32(value);
			return TimeSpan.FromSeconds(val).ToString("mm':'ss");
		}

		public object ConvertBack(object value, Type targetType, object parameter, System.Globalization.CultureInfo culture)
		{
			return null;
		}
		#endregion
	}
}
