namespace UWPClient.Messages
{
    using Newtonsoft.Json;
    using System;

    public class JsonMessageFactory
    {
        public static JsonMessage DeserializeJsonMessage(string message)
        {
            JsonMessage jsonMessage = JsonConvert.DeserializeObject<JsonMessage>(message);
            string messageType = jsonMessage.messageType;

            if (messageType.Equals("LoginMessage"))
            {
                return JsonConvert.DeserializeObject<LoginMessage>(message);
            }
            else if (messageType.Equals("LobbyStateMessage"))
            {
                return JsonConvert.DeserializeObject<LobbyStateMessage>(message);
            }

            return null;
        }

        public static LoginMessage CreateLoginMessage(string userName)
        {
            LoginMessage loginMessage = new LoginMessage();
            setMessageType(loginMessage);
            loginMessage.userName = userName;
            return loginMessage;
        }

        public static LobbyStateMessage CreateLobbyStateMessage(string[] gameNames, int[] gameStatuses, int[] gamePlayerCounts, string[] users)
        {
            LobbyStateMessage lobbyStateMessage = new LobbyStateMessage();
            setMessageType(lobbyStateMessage);
            lobbyStateMessage.gameNames = gameNames;
            lobbyStateMessage.gameStatuses = gameStatuses;
            lobbyStateMessage.gamePlayerCounts = gamePlayerCounts;
            lobbyStateMessage.users = users;
            return lobbyStateMessage;
        }

        private static void setMessageType(JsonMessage message)
        {
            message.messageType = message.GetType().Name;
        }
    }
}
