namespace UWPClient
{
    using Windows.Networking;
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
    using System;
    using System.Collections.Concurrent;
    using System.Runtime.InteropServices.WindowsRuntime;
    using System.Threading.Tasks;

    public delegate void MessageCallback(string message);

    public class TcpMessager
    {
        private static StreamSocketListener socketListener;

        private static MessageCallback callback;

        private static async void HandleConnectionReceived(StreamSocketListener socketListener, StreamSocketListenerConnectionReceivedEventArgs args)
        {
            IBuffer lengthInputBuffer = new Windows.Storage.Streams.Buffer(4);
            IBuffer lengthOutputBuffer = await args.Socket.InputStream.ReadAsync(lengthInputBuffer, 4, InputStreamOptions.None);
            byte[] lengthBytes = WindowsRuntimeBufferExtensions.ToArray(lengthOutputBuffer);
            int lengthSigned = (lengthBytes[0] << 24) | (lengthBytes[1] << 16) | (lengthBytes[2] << 8) | lengthBytes[3];
            uint length = (uint)lengthSigned;

            IBuffer inputBuffer = new Windows.Storage.Streams.Buffer(length);
            IBuffer outputBuffer = await args.Socket.InputStream.ReadAsync(inputBuffer, length, InputStreamOptions.None);
            byte[] messageBytes = WindowsRuntimeBufferExtensions.ToArray(outputBuffer);
            char[] messageChars = new char[messageBytes.Length];
            for (int i = 0; i < messageBytes.Length; i++)
            {
                messageChars[i] = (char)messageBytes[i];
            }
            string message = new string(messageChars);

            callback(message);
        }

        public static async Task Init(int port)
        {
            socketListener = new StreamSocketListener();
            await socketListener.BindServiceNameAsync(port.ToString());
            callback = defaultCallback;
            socketListener.ConnectionReceived += HandleConnectionReceived;
        }

        public static void BindMessageCallback(MessageCallback cb)
        {
            callback = cb;
        }

        private static void defaultCallback(String message)
        {

        }

        public static async Task SendMessageAsync(string message, string hostname, int port)
        {
            using (StreamSocket socket = new StreamSocket())
            {
                string portString = port.ToString();
                HostName hostNameObj = new HostName(hostname);
                await socket.ConnectAsync(hostNameObj, portString);

                byte[] bytes = new byte[4];
                bytes[0] = (byte)((message.Length >> 24) & 0xFF);
                bytes[1] = (byte)((message.Length >> 16) & 0xFF);
                bytes[2] = (byte)((message.Length >> 8) & 0xFF);
                bytes[3] = (byte)(message.Length & 0xFF);

                IBuffer buffer = Windows.Security.Cryptography.CryptographicBuffer.CreateFromByteArray(bytes);
                await socket.OutputStream.WriteAsync(buffer);

                byte[] messageBytes = new byte[message.Length];
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
