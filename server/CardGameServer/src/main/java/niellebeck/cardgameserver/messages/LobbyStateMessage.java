package niellebeck.cardgameserver.messages;

public class LobbyStateMessage extends JsonMessage {
    public String[] gameNames;
    public int[] gameStatuses;
    public int[] gamePlayerCounts;
    public String[] users;
}
