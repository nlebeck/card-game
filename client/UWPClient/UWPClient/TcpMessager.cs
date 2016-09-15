namespace UWPClient
{
    using Windows.Networking;
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
    using System;
    using System.Collections.Concurrent;
    using System.Threading.Tasks;

    public class TcpMessager
    {
        private static ConcurrentDictionary<int, TcpMessager> tcpMessagerDictionary = new ConcurrentDictionary<int, TcpMessager>();

        public static TcpMessager GetTcpMessager(int listeningPort)
        {
            if (!tcpMessagerDictionary.ContainsKey(listeningPort))
            {
                tcpMessagerDictionary[listeningPort] = new TcpMessager(listeningPort);
            }
            return tcpMessagerDictionary[listeningPort];
        }

        private TcpMessager(int listeningPort)
        {

        }

        public async Task SendMessageAsync(string message, string hostname, int port)
        {
            using (StreamSocket socket = new StreamSocket())
            {
                string portString = port.ToString();
                HostName hostNameObj = new HostName(hostname);
                await socket.ConnectAsync(hostNameObj, portString);

                int messageLength = message.Length;
                byte[] bytes = new byte[4];
                bytes[0] = (byte)((messageLength >> 24) & 0xFF);
                bytes[1] = (byte)((messageLength >> 16) & 0xFF);
                bytes[2] = (byte)((messageLength >> 8) & 0xFF);
                bytes[3] = (byte)(messageLength & 0xFF);

                IBuffer buffer = Windows.Security.Cryptography.CryptographicBuffer.CreateFromByteArray(bytes);
                await socket.OutputStream.WriteAsync(buffer);

                byte[] messageBytes = new byte[messageLength];
                char[] messageChars = message.ToCharArray();
                for (int i = 0; i < message.Length; i++)
                {
                    messageBytes[i] = (byte)messageChars[i];
                }

                IBuffer messageBuffer = Windows.Security.Cryptography.CryptographicBuffer.CreateFromByteArray(messageBytes);
                await socket.OutputStream.WriteAsync(messageBuffer);
            }
        }
    }
}
