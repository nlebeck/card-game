package niellebeck.cardgameserver;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import niellebeck.cardgameserver.messages.JsonMessage;
import niellebeck.cardgameserver.messages.JsonMessageFactory;
import niellebeck.cardgameserver.messages.LobbyStateMessage;
import niellebeck.cardgameserver.messages.LoginMessage;
import niellebeck.cardgameserver.messaging.JsonMessageCallback;
import niellebeck.cardgameserver.messaging.JsonMessenger;

/**
 * The server for the card game
 *
 */
public class CardGameServer
{
    private static final int SERVER_PORT = 8080;
    
    private static List<Game> games;
    private static List<String> users;
    
    public static void main( String[] args )
    {        
        try {
            JsonMessenger.init(SERVER_PORT);
        }
        catch (IOException e) {
            System.err.println("Error creating TcpMessenger: " + e);
            System.exit(1);
        }
        
        games = new ArrayList<Game>();
        users = new ArrayList<String>();
        
        JsonMessenger.startMessageLoop(new JsonMessageCallback() {
            public void callback(SocketAddress address, JsonMessage jsonMessage) {
                String addressStr = address.toString().substring(1).split(":")[0];
                System.out.println("Message received from " + addressStr + " of type " + jsonMessage.messageType);
                
                if (jsonMessage.messageType.equals("LoginMessage")) {
                    LoginMessage loginMessage = (LoginMessage)jsonMessage;
                    
                    String userName = loginMessage.userName;
                    users.add(userName);
                    
                    LobbyStateMessage lobbyStateMessage = generateLobbyStateMessage();
                    JsonMessenger.sendMessage(address, lobbyStateMessage);
                }
            }
        });
    }
    
    public static LobbyStateMessage generateLobbyStateMessage() {
        int numGames = games.size();
        String[] gameNames = new String[numGames];
        int[] gameStatuses = new int[numGames];
        int[] gamePlayerCounts = new int[numGames];
        for (int i = 0; i < numGames; i++) {
            Game game = games.get(i);
            gameNames[i] = game.getGameName();
            gameStatuses[i] = (game.isStarted() ? 1 : 0);
            gamePlayerCounts[i] = game.getNumPlayers();
        }
        
        String[] usersArray = users.toArray(new String[0]);
        
        LobbyStateMessage lobbyStateMessage = JsonMessageFactory.createLobbyStateMessage(gameNames, gameStatuses, gamePlayerCounts, usersArray);
        return lobbyStateMessage;
    }
}
