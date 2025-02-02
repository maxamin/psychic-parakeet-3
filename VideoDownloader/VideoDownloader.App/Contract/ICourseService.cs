using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using VideoDownloader.App.Model;

namespace VideoDownloader.App.Contract
{
	public interface ICourseService
	{

		Task<bool> ProcessNoncachedProductsJsonAsync();

	    Task<bool> ProcessCachedProductsAsync();

        string CachedProductsJson { get; }

	    string Cookies { get; set; }

	    Dictionary<string, List<CourseDescription>> CoursesByToolName { get; set; }

	    Task DownloadAsync(string productId, IProgress<CourseDownloadingProgressArguments> downloadingProgress, IProgress<int> timeoutProgress, CancellationToken token);

	    Task<List<CourseDescription>> GetToolCourses(string toolName);
	}
}
