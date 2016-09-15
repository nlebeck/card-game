package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class TcpMessager {
    
    private static Map<Integer, TcpMessager> tcpMessagerMap = new HashMap<Integer, TcpMessager>();
    
    private ServerSocket serverSocket;
    
    public static TcpMessager getTcpMessager(int serverPort) throws IOException {
        if (!tcpMessagerMap.containsKey(serverPort)) {
            tcpMessagerMap.put(serverPort, new TcpMessager(serverPort));
        }
        return tcpMessagerMap.get(serverPort);
    }
    
    private TcpMessager(int serverPort) throws IOException {
        this.serverSocket = new ServerSocket(serverPort);
    }
    
    public void sendMessage(String message, String hostname, int port) throws IOException {
        //TODO: implement
    }
    
    public String receiveMessage() throws IOException {
        Socket socket = serverSocket.accept();
        byte[] messageLengthBytes = new byte[4];
        socket.getInputStream().read(messageLengthBytes);
        int messageLength = (messageLengthBytes[0] << 24) | (messageLengthBytes[1] << 16) | (messageLengthBytes[2] << 8) | messageLengthBytes[3];
        byte[] message = new byte[messageLength];
        socket.getInputStream().read(message);
        char[] messageChars = new char[messageLength];
        for (int i = 0; i < message.length; i++) {
            messageChars[i] = (char)message[i];
        }
        return String.valueOf(messageChars, 0, messageChars.length);
    }
}
