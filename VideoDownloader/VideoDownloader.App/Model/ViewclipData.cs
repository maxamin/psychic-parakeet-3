using Newtonsoft.Json;

namespace VideoDownloader.App.Model
{
    class ViewclipData
    {
        [JsonProperty("author")]
        public string Author { get; set; }

        [JsonProperty("clipIndex")]
        public int ClipIndex { get; set; }

        [JsonProperty("courseName")]
        public string CourseName { get;set;}

        [JsonProperty("includeCaptions")]
        public bool IncludeCaptions { get;set;}

        [JsonProperty("locale")]
        public string Locale { get; set; }

        [JsonProperty("mediaType")]
        public string MediaType { get; set; }

        [JsonProperty("moduleName")]
        public string ModuleName { get; set; }

        [JsonProperty("quality")]
        public string Quality { get; set; }

    }
}
