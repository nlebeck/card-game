﻿using System;
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
        private TcpMessageClient client;

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
                client = new TcpMessageClient();
                await client.Connect(this.hostNameTextBox.Text, this.portTextBox.Text);
                client.StartReadLoop(HandleMessage);
            }

            try
            {
                await client.SendMessageAsync("Hello world from a new client!");
            }
            catch (Exception exception)
            {
                errorMessage = exception.Message;
            }

            this.errorTextBlock.Text = errorMessage;
        }

        private async void HandleMessage(string message)
        {
            await dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                this.responseTextBlock.Text = "Message from server: " + message;
            });
        }
    }
}
