package niellebeck.cardgameserver;

import java.io.IOException;
import java.net.SocketAddress;

import niellebeck.cardgameserver.messages.JsonMessage;
import niellebeck.cardgameserver.messages.JsonMessageFactory;
import niellebeck.cardgameserver.messages.LobbyStateMessage;
import niellebeck.cardgameserver.messaging.JsonMessageCallback;
import niellebeck.cardgameserver.messaging.JsonMessenger;

/**
 * The server for the card game
 *
 */
public class CardGameServer
{
    private static final int SERVER_PORT = 8080;
    
    public static void main( String[] args )
    {        
        try {
            JsonMessenger.init(SERVER_PORT);
        }
        catch (IOException e) {
            System.err.println("Error creating TcpMessenger: " + e);
            System.exit(1);
        }
        
        JsonMessenger.startMessageLoop(new JsonMessageCallback() {
            public void callback(SocketAddress address, JsonMessage jsonMessage) {
                String addressStr = address.toString().substring(1).split(":")[0];
                System.out.println("Message received from " + addressStr + " of type " + jsonMessage.messageType);
                String[] gameNames = new String[2];
                gameNames[0] = "Test game 1";
                gameNames[1] = "Test game 2";
                int[] gameStatuses = new int[2];
                gameStatuses[0] = 0;
                gameStatuses[1] = 1;
                String[] users = new String[2];
                users[0] = "Test user 1";
                users[1] = "Test user 2";
                LobbyStateMessage lobbyStateMessage = JsonMessageFactory.createLobbyStateMessage(gameNames, gameStatuses, users);
                JsonMessenger.sendMessage(address, lobbyStateMessage);
            }
        });
    }
}
