namespace UWPClient
{
    using Windows.Networking;
    using Windows.Networking.Sockets;
    using Windows.Storage.Streams;
    using System;
    using System.Collections.Concurrent;
    using System.Runtime.InteropServices.WindowsRuntime;
    using System.Threading;
    using System.Threading.Tasks;

    public delegate void MessageCallback(string message);

    public class TcpMessageClient
    {
        private volatile StreamSocket socket;
        private ConcurrentQueue<string> messageQueue;
        private EventWaitHandle callbackWaitHandle;
        private MessageCallback callback;

        public async Task Connect(string serverHostname, string serverPort)
        {
            socket = new StreamSocket();
            messageQueue = new ConcurrentQueue<string>();
            callbackWaitHandle = new EventWaitHandle(false, EventResetMode.AutoReset);
            callback = null;

            HostName hostNameObj = new HostName(serverHostname);
            await socket.ConnectAsync(hostNameObj, serverPort);
        }

        public void Close()
        {
            socket.Dispose();
        }

        public void RegisterCallback(MessageCallback callback)
        {
            this.callback = callback;
            callbackWaitHandle.Set();
        }

        public void DeregisterCallback()
        {
            this.callback = null;
        }

        public void StartReceivingMessages()
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
                    messageQueue.Enqueue(message);
                    callbackWaitHandle.Set();
                }
            }, TaskCreationOptions.LongRunning);

            Task runCallbackTask = new Task(() =>
            {
                while (true)
                {
                    callbackWaitHandle.WaitOne();

                    while (messageQueue.Count > 0 && callback != null)
                    {
                        string message;
                        bool dequeued = messageQueue.TryDequeue(out message);
                        if (dequeued)
                        {
                            callback(message);
                        }
                    }
                }
            }, TaskCreationOptions.LongRunning);

            readLoopTask.Start();
            runCallbackTask.Start();
        }

        public async Task SendMessageAsync(string message)
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
