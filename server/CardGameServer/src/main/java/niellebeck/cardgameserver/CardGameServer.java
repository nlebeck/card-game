package niellebeck.cardgameserver;

import java.io.IOException;

import niellebeck.cardgameserver.messaging.MessageCallback;
import niellebeck.cardgameserver.messaging.TcpMessager;

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
            TcpMessager.init(SERVER_PORT);
        }
        catch (IOException e) {
            System.err.println("Error creating TcpMessager: " + e);
            System.exit(1);
        }
        
        TcpMessager.startMessageLoop(new MessageCallback() {
            public void callback(String address, String message) {
                System.out.println("Message received from " + address + ": " + message);
                try {
                    TcpMessager.sendMessage("Message received: " + message, address, 8079);
                }
                catch (IOException e) {
                    System.err.println("Error sending response: " + e);
                }
            }
        });
    }
}
