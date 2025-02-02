namespace VideoDownloader.App.Model
{
	public class CourseDownloadingProgressArguments
	{
		public string ClipName { get; set; }

		public int CourseProgress { get; set; }

		public int ClipProgress { get; set; }

		public string CurrentAction { get; set; }
	}
}
