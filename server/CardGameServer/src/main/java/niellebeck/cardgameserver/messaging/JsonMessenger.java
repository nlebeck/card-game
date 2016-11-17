package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.SocketAddress;

import com.google.gson.Gson;

import niellebeck.cardgameserver.messages.JsonMessage;
import niellebeck.cardgameserver.messages.JsonMessageFactory;

public class JsonMessenger {  
    private static Gson gson;
    
    public static void init(int listeningPort) throws IOException {
        gson = new Gson();
        TcpMessenger.init(listeningPort);
    }
    
    public static void startMessageLoop(JsonMessageCallback callback) {
        TcpMessenger.startMessageLoop(new MessageCallback() {
            public void callback(SocketAddress address, String message) {
                JsonMessage jsonMessage = JsonMessageFactory.deserializeJsonMessage(message);
                callback.callback(address, jsonMessage);
            }
        });
    }
    
    public static void sendMessage(SocketAddress address, JsonMessage jsonMessage) {
        String message = gson.toJson(jsonMessage);
        TcpMessenger.sendMessage(address, message);
    }
}
