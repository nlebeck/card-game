namespace UWPClient
{
    using System.IO;
    using System.Threading.Tasks;
    using Newtonsoft.Json;
    using Messages;

    public class JsonMessageClient
    {
        public delegate void JsonMessageCallback(JsonMessage jsonMessage);

        private static JsonMessageClient singleton;

        private TcpMessageClient tcpMessageClient;
        private JsonMessageCallback callback;
        private MessageCallback tcpMessageClientCallback;

        public JsonMessageClient()
        {
            tcpMessageClient = new TcpMessageClient();
            callback = null;

            tcpMessageClientCallback = (string message) =>
            {
                JsonMessage jsonMessage = JsonMessageFactory.DeserializeJsonMessage(message);
                callback(jsonMessage);
            };
        }

        public async Task Connect(string serverHostname, string serverPort)
        {
            await tcpMessageClient.Connect(serverHostname, serverPort);
        }

        public void Close()
        {
            tcpMessageClient.Close();
        }

        public void RegisterCallback(JsonMessageCallback callback)
        {
            this.callback = callback;
            tcpMessageClient.RegisterCallback(tcpMessageClientCallback);
        }

        public void DeregisterCallback()
        {
            tcpMessageClient.DeregisterCallback();
            this.callback = null;
        }

        public void StartReceivingMessages()
        {
            tcpMessageClient.StartReceivingMessages();
        }

        public async Task SendMessageAsync(JsonMessage jsonMessage)
        {
            string message = JsonConvert.SerializeObject(jsonMessage);
            await tcpMessageClient.SendMessageAsync(message);
        }

        public static JsonMessageClient GetSingleton()
        {
            if (singleton == null)
            {
                singleton = new JsonMessageClient();
            }
            return singleton;
        }
    }
}
