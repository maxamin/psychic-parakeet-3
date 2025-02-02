namespace VideoDownloader.App.Model
{
    public class AllProducts
	{
		public dynamic General { get; set; }
		public dynamic Banners { get; set; }
		public dynamic Menus { get; set; }
		public dynamic BreadCrumbs { get; set; }
		public dynamic Pagination { get; set; }
		public dynamic Facets { get; set; }
		public ResultSet[] ResultSets { get; set; }
		public dynamic ResultCount { get; set; }

	}
}
