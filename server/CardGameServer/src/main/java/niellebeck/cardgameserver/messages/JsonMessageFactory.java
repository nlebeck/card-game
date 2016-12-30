package niellebeck.cardgameserver.messages;

import com.google.gson.Gson;

public class JsonMessageFactory {
    public static JsonMessage deserializeJsonMessage(String message) {
        Gson gson = new Gson();
        JsonMessage jsonMessage = gson.fromJson(message, JsonMessage.class);
        String messageType = jsonMessage.messageType;

        if (messageType.equals("LobbyStateMessage")) {
            return gson.fromJson(message, LobbyStateMessage.class);
        }
        if (messageType.equals("LoginMessage")) {
            return gson.fromJson(message, LoginMessage.class);
        }
        if (messageType.equals("LoginReplyMessage")) {
            return gson.fromJson(message, LoginReplyMessage.class);
        }

        return null;
    }

    public static LobbyStateMessage createLobbyStateMessage(String[] gameNames, int[] gameStatuses, int[] gamePlayerCounts, String[] users) {
        LobbyStateMessage message = new LobbyStateMessage();
        message.messageType = "LobbyStateMessage";
        message.gameNames = gameNames;
        message.gameStatuses = gameStatuses;
        message.gamePlayerCounts = gamePlayerCounts;
        message.users = users;
        return message;
    }

    public static LoginMessage createLoginMessage(String userName) {
        LoginMessage message = new LoginMessage();
        message.messageType = "LoginMessage";
        message.userName = userName;
        return message;
    }

    public static LoginReplyMessage createLoginReplyMessage(int response) {
        LoginReplyMessage message = new LoginReplyMessage();
        message.messageType = "LoginReplyMessage";
        message.response = response;
        return message;
    }

}
