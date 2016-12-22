using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using UWPClient.Messages;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Networking;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;
using Windows.UI.Core;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;

// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace UWPClient
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        private CoreDispatcher dispatcher;
        private JsonMessageClient client;

        public MainPage()
        {
            this.InitializeComponent();

            dispatcher = CoreWindow.GetForCurrentThread().Dispatcher;
            client = null;
        }

        private async void button_Click(object sender, RoutedEventArgs e)
        {
            string errorMessage = "";

            if (client == null)
            {
                client = JsonMessageClient.GetSingleton();
                string serverInfo = serverTextBox.Text;
                string[] serverInfoSplit = serverInfo.Split(new char[] { ':' });
                string hostName = serverInfoSplit[0];
                string port = serverInfoSplit[1];
                await client.Connect(hostName, port);
                client.StartReceivingMessages();
                client.RegisterCallback(HandleMessage);
            }

            try
            {
                LoginMessage loginMessage = JsonMessageFactory.CreateLoginMessage("TestUser1");
                await client.SendMessageAsync(loginMessage);
            }
            catch (Exception exception)
            {
                errorMessage = exception.Message;
            }

            this.errorTextBlock.Text = errorMessage;
        }

        private async void HandleMessage(JsonMessage jsonMessage)
        {
            string text = "";

            if (jsonMessage.GetType() == typeof(LoginReplyMessage))
            {
                LoginReplyMessage loginReplyMessage = (LoginReplyMessage)jsonMessage;
                if (loginReplyMessage.response == 0)
                {
                    await dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
                    {
                        client.DeregisterCallback();
                        this.Frame.Navigate(typeof(LobbyPage));
                    });
                }
                else
                {
                    text = "Login failed";
                }
            }
            else
            {
                text = "Message received from server of type " + jsonMessage.messageType;
            }

            await dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                this.responseTextBlock.Text = text;
            });
        }
    }
}
