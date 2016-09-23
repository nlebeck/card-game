using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using System.Threading.Tasks;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.Networking;
using Windows.Networking.Sockets;
using Windows.Storage.Streams;
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
        public MainPage()
        {
            this.InitializeComponent();

            TcpMessager.Init(8079).Wait();
            TcpMessager.BindMessageCallback(HandleMessage);
        }

        private async void button_Click(object sender, RoutedEventArgs e)
        {
            string errorMessage = "";

            try
            {
                await TcpMessager.SendMessageAsync("Hello world!", this.hostNameTextBox.Text, int.Parse(this.portTextBox.Text));
            }
            catch (Exception exception)
            {
                errorMessage = exception.Message;
            }

            this.errorTextBlock.Text = errorMessage;
        }

        private void HandleMessage(string address, string message)
        {
            this.responseTextBlock.Text = "Message from " + address + ": " + message;
        }
    }
}
