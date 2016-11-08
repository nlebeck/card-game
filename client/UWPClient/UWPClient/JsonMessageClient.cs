namespace UWPClient
{
    using System.IO;
    using System.Threading.Tasks;
    using Newtonsoft.Json;
    using Messages;

    public class JsonMessageClient
    {
        public delegate void JsonMessageCallback(JsonMessage jsonMessage);

        private TcpMessageClient tcpMessageClient;

        public JsonMessageClient()
        {
            tcpMessageClient = new TcpMessageClient();
        }

        public async Task Connect(string serverHostname, string serverPort)
        {
            await tcpMessageClient.Connect(serverHostname, serverPort);
        }

        public void Close()
        {
            tcpMessageClient.Close();
        }

        public void StartReadLoop(JsonMessageCallback callback)
        {
            tcpMessageClient.StartReadLoop((string message) =>
            {
                JsonMessage jsonMessage = JsonConvert.DeserializeObject<JsonMessage>(message);
                callback(jsonMessage);
            });
        }

        public async Task SendMessageAsync(JsonMessage jsonMessage)
        {
            string message = JsonConvert.SerializeObject(jsonMessage);
            await tcpMessageClient.SendMessageAsync(message);
        }
    }
}
