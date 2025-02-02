using GalaSoft.MvvmLight.Messaging;

namespace VideoDownloader.App.BL.Messages
{
    class ExceptionThrownMessage: MessageBase
    {
        public ExceptionThrownMessage(string messageText)
        {
            Text = messageText;
        }

        public string Text { get; set; }
    }
}
