namespace UWPClient
{
    using System.IO;
    using System.Runtime.Serialization.Json;
    using System.Threading.Tasks;
    using Messages;

    public class JsonMessageClient
    {
        public delegate void JsonMessageCallback(JsonMessage jsonMessage);

        private TcpMessageClient tcpMessageClient;

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
                MemoryStream stream = new MemoryStream();
                StreamWriter streamWriter = new StreamWriter(stream);
                streamWriter.Write(message);

                stream.Position = 0;
                DataContractJsonSerializer serializer = new DataContractJsonSerializer(typeof(JsonMessage));
                JsonMessage jsonMessage = (JsonMessage)serializer.ReadObject(stream);
                callback(jsonMessage);
            });
        }

        public async Task SendMessageAsync(JsonMessage jsonMessage)
        {
            DataContractJsonSerializer serializer = new DataContractJsonSerializer(jsonMessage.GetType());
            MemoryStream stream = new MemoryStream();
            serializer.WriteObject(stream, jsonMessage);

            stream.Position = 0;
            StreamReader streamReader = new StreamReader(stream);
            string message = streamReader.ReadToEnd();

            await tcpMessageClient.SendMessageAsync(message);
        }
    }
}
