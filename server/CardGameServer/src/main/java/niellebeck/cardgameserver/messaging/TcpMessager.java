package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class TcpMessager {
    
    private static ConcurrentHashMap<Integer, TcpMessager> tcpMessagerMap = new ConcurrentHashMap<Integer, TcpMessager>();
    
    private ServerSocket serverSocket;
    
    public static TcpMessager getTcpMessager(int listeningPort) throws IOException {
        if (!tcpMessagerMap.containsKey(listeningPort)) {
            tcpMessagerMap.put(listeningPort, new TcpMessager(listeningPort));
        }
        return tcpMessagerMap.get(listeningPort);
    }
    
    private TcpMessager(int listeningPort) throws IOException {
        this.serverSocket = new ServerSocket(listeningPort);
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
