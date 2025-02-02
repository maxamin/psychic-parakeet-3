using System;
using Newtonsoft.Json;

namespace VideoDownloader.App.Model
{
	public class CourseDescription
	{
        [JsonProperty("authors")]
        public Author[] Authors { get; set; }

        [JsonProperty("imageVersion")]
        public int ImageVersion { get; set; }

        [JsonProperty("description")]
        public string Description { get; set; }

	    [JsonProperty("courseName")]
	    public string CourseName { get; set; }

        [JsonProperty("duration")]
        public string Duration { get; set; }

        [JsonProperty("hasTranscript")]
        public bool HasTranscript { get; set; }

        [JsonProperty("prodId")]
        public string Id { get; set; }

        [JsonProperty("tools")]
        public string Tools { get; set; }

        [JsonProperty("modules")]
        public Module[] Modules { get; set; }

        [JsonProperty("url")]
        public string Url { get; set; }

        [JsonProperty("publishedDate")]
        public DateTime PublishedDate { get; set; }

        [JsonProperty("averageRating")]
        public double AverageRating { get; set; }

        [JsonProperty("ratingCount")]
        public int RatingCount { get; set; }

        [JsonProperty("retired")]
        public object Retired { get; set; }

        [JsonProperty("subjects")]
        public string Subjects { get; set; }

        [JsonProperty("skillLevels")]
        public object SkillLevels { get; set; }

        [JsonProperty("title")]
        public string Title { get; set; }

        [JsonProperty("updatedDate")]
        public DateTime UpdatedDate { get; set; }

        [JsonProperty("loc")]
        public string Location { get; set; }

        [JsonProperty("keywords")]
        public string Keywords { get; set; }

	    [JsonProperty("categories")]
	    public string Categories { get; set; }

        public bool CheckedForDownloading { get; set; }
	}
}
