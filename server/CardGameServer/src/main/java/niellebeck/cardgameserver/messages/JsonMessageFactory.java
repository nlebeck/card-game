package niellebeck.cardgameserver.messages;

import com.google.gson.Gson;

public class JsonMessageFactory {
    public static JsonMessage deserializeJsonMessage(String message) {
        Gson gson = new Gson();
        JsonMessage jsonMessage = gson.fromJson(message, JsonMessage.class);
        String messageType = jsonMessage.messageType;
        
        if (messageType.equals("LoginMessage")) {
            return gson.fromJson(message, LoginMessage.class);
        }
        else if (messageType.equals("LobbyStateMessage")) {
            return gson.fromJson(message, LobbyStateMessage.class);
        }
        
        return null;
    }
    
    public static LoginMessage createLoginMessage(String userName) {
        LoginMessage loginMessage = new LoginMessage();
        setMessageType(loginMessage);
        loginMessage.userName = userName;
        return loginMessage;
    }
    
    public static LobbyStateMessage createLobbyStateMessage(String[] gameNames, int[] gameStatuses, String[] users) {
        LobbyStateMessage lobbyStateMessage = new LobbyStateMessage();
        setMessageType(lobbyStateMessage);
        lobbyStateMessage.gameNames = gameNames;
        lobbyStateMessage.gameStatuses = gameStatuses;
        lobbyStateMessage.users = users;
        return lobbyStateMessage;
    }
    
    private static void setMessageType(JsonMessage message) {
        message.messageType = message.getClass().getSimpleName();
    }
}
