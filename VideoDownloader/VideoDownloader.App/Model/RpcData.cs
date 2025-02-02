namespace VideoDownloader.App.Model
{
    class RpcData
    {
        public bool Success { get; set; }

        public PayloadRpc Payload { get; set; }

        public dynamic Trace { get; set; }
    }
}
