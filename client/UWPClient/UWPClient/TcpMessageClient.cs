namespace UWPClient
{
    using Windows.Networking;
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
    using Windows.UI.Core;
    using System;
    using System.Collections.Concurrent;
    using System.Runtime.InteropServices.WindowsRuntime;
    using System.Threading;
    using System.Threading.Tasks;

    public delegate void MessageCallback(string address, string message);

    public class TcpMessageClient
    {
        private volatile StreamSocket socket;

        public async Task Connect(string serverHostname, string serverPort)
        {
            socket = new StreamSocket();

            HostName hostNameObj = new HostName(serverHostname);
            await socket.ConnectAsync(hostNameObj, serverPort);
        }

        public void Close()
        {
            socket.Dispose();
        }

        public void StartReadLoop(MessageCallback callback)
        {
            Task readLoopTask = new Task(async () =>
            {
                while (true)
                {
                    IBuffer lengthInputBuffer = new Windows.Storage.Streams.Buffer(4);
                    IBuffer lengthOutputBuffer = await socket.InputStream.ReadAsync(lengthInputBuffer, 4, InputStreamOptions.None);
                    byte[] lengthBytes = WindowsRuntimeBufferExtensions.ToArray(lengthOutputBuffer);
                    int lengthSigned = (lengthBytes[0] << 24) | (lengthBytes[1] << 16) | (lengthBytes[2] << 8) | lengthBytes[3];
                    uint length = (uint)lengthSigned;

                    IBuffer inputBuffer = new Windows.Storage.Streams.Buffer(length);
                    IBuffer outputBuffer = await socket.InputStream.ReadAsync(inputBuffer, length, InputStreamOptions.None);
                    byte[] messageBytes = WindowsRuntimeBufferExtensions.ToArray(outputBuffer);
                    char[] messageChars = new char[messageBytes.Length];
                    for (int i = 0; i < messageBytes.Length; i++)
                    {
                        messageChars[i] = (char)messageBytes[i];
                    }
                    string message = new string(messageChars);

                    string address = socket.Information.RemoteAddress.CanonicalName;
                    callback(address, message);
                }
            }, TaskCreationOptions.LongRunning);
            readLoopTask.Start();
        }

        public async Task SendMessageAsync(string message, MessageCallback cb)
        {
            byte[] messageLengthBytes = new byte[4];
            messageLengthBytes[0] = (byte)((message.Length >> 24) & 0xFF);
            messageLengthBytes[1] = (byte)((message.Length >> 16) & 0xFF);
            messageLengthBytes[2] = (byte)((message.Length >> 8) & 0xFF);
            messageLengthBytes[3] = (byte)(message.Length & 0xFF);

            IBuffer lengthBuffer = Windows.Security.Cryptography.CryptographicBuffer.CreateFromByteArray(messageLengthBytes);
            await socket.OutputStream.WriteAsync(lengthBuffer);

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
