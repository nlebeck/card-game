namespace UWPClient.Messages
{

    using Newtonsoft.Json;

    public class JsonMessageFactory
    {
        public static JsonMessage DeserializeJsonMessage(string message)
        {
            JsonMessage jsonMessage = JsonConvert.DeserializeObject<JsonMessage>(message);
            string messageType = jsonMessage.messageType;

            if (messageType.Equals("LobbyStateMessage"))
            {
                return JsonConvert.DeserializeObject<LobbyStateMessage>(message);
            }
            if (messageType.Equals("LoginMessage"))
            {
                return JsonConvert.DeserializeObject<LoginMessage>(message);
            }
            if (messageType.Equals("LoginReplyMessage"))
            {
                return JsonConvert.DeserializeObject<LoginReplyMessage>(message);
            }

            return null;
        }

        public static LobbyStateMessage CreateLobbyStateMessage(string[] gameNames, int[] gameStatuses, int[] gamePlayerCounts, string[] users)
        {
            LobbyStateMessage message = new LobbyStateMessage();
            message.messageType = "LobbyStateMessage";
            message.gameNames = gameNames;
            message.gameStatuses = gameStatuses;
            message.gamePlayerCounts = gamePlayerCounts;
            message.users = users;
            return message;
        }

        public static LoginMessage CreateLoginMessage(string userName)
        {
            LoginMessage message = new LoginMessage();
            message.messageType = "LoginMessage";
            message.userName = userName;
            return message;
        }

        public static LoginReplyMessage CreateLoginReplyMessage(int response)
        {
            LoginReplyMessage message = new LoginReplyMessage();
            message.messageType = "LoginReplyMessage";
            message.response = response;
            return message;
        }

    }
}
