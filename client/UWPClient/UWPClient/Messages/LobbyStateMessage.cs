namespace UWPClient.Messages
{

    public class LobbyStateMessage : JsonMessage
    {
        public string[] gameNames;
        public int[] gameStatuses;
        public int[] gamePlayerCounts;
        public string[] users;
    }
}
