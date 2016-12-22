namespace UWPClient
{
    using System;
    using Messages;
    using Windows.UI.Core;
    using Windows.UI.Xaml.Controls;

    public sealed partial class LobbyPage : Page
    {
        private CoreDispatcher dispatcher;
        private JsonMessageClient client;

        public LobbyPage()
        {
            this.InitializeComponent();

            dispatcher = CoreWindow.GetForCurrentThread().Dispatcher;
            client = JsonMessageClient.GetSingleton();
            client.RegisterCallback(HandleMessage);
        }

        private async void HandleMessage(JsonMessage jsonMessage)
        {
            string text = "";

            if (jsonMessage.GetType() == typeof(LobbyStateMessage))
            {
                LobbyStateMessage lobbyStateMessage = (LobbyStateMessage)jsonMessage;
                text = "Games:\n";
                for (int i = 0; i < lobbyStateMessage.gameNames.Length; i++)
                {
                    text += lobbyStateMessage.gameNames[i]
                        + " players: " + lobbyStateMessage.gamePlayerCounts[i]
                        + " status: " + (lobbyStateMessage.gameStatuses[i] == 1 ? "started" : "not started")
                        + "\n";
                }
                text += "Users:\n";
                for (int i = 0; i < lobbyStateMessage.users.Length; i++)
                {
                    text += lobbyStateMessage.users[i];
                }
            }

            await dispatcher.RunAsync(CoreDispatcherPriority.Normal, () =>
            {
                debugTextBlock.Text = text;
            });
        }
    }
}
