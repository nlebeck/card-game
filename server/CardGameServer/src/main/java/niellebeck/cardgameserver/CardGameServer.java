package niellebeck.cardgameserver;

import java.io.IOException;
import java.net.SocketAddress;

import niellebeck.cardgameserver.messages.JsonMessage;
import niellebeck.cardgameserver.messages.LoginReplyMessage;
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
                LoginReplyMessage replyMessage = new LoginReplyMessage();
                replyMessage.messageType = replyMessage.getClass().getSimpleName();
                replyMessage.reply = "ok";
                JsonMessenger.sendMessage(address, replyMessage);
            }
        });
    }
}
