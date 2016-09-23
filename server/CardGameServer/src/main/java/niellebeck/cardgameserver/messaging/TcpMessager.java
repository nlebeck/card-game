package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class TcpMessager {
        
    private static ServerSocket serverSocket = null;
    
    public static void init(int listeningPort) throws IOException {
        serverSocket = new ServerSocket(listeningPort);
    }
    
    public static void startMessageLoop(MessageCallback callback) {
        while(true) {
            String message = null;
            String address = null;
            try {
                String[] result = receiveMessage();
                address = result[0];
                message = result[1];
            }
            catch (IOException e) {
                System.err.println("Error receiving message: " + e);
            }
            
            callback.callback(address, message);
        }
    }
    
    private static String[] receiveMessage() throws IOException {
        Socket socket = serverSocket.accept();
        
        String address = socket.getInetAddress().getHostAddress();
        
        byte[] messageLengthBytes = new byte[4];
        socket.getInputStream().read(messageLengthBytes);
        int messageLength = (messageLengthBytes[0] << 24) | (messageLengthBytes[1] << 16) | (messageLengthBytes[2] << 8) | messageLengthBytes[3];
        
        byte[] messageBytes = new byte[messageLength];
        socket.getInputStream().read(messageBytes);
        char[] messageChars = new char[messageLength];
        for (int i = 0; i < messageBytes.length; i++) {
            messageChars[i] = (char)messageBytes[i];
        }
        String message = String.valueOf(messageChars, 0, messageChars.length);
        
        socket.close();
        
        String[] result = new String[2];
        result[0] = address;
        result[1] = message;
        
        return result;
    }
    
    public static void sendMessage(String message, String hostname, int port) throws IOException {
        Socket socket = new Socket(hostname, port);
        
        byte[] bytes = new byte[4];
        bytes[0] = (byte)((message.length() >> 24) & 0xFF);
        bytes[1] = (byte)((message.length() >> 16) & 0xFF);
        bytes[2] = (byte)((message.length() >> 8) & 0xFF);
        bytes[3] = (byte)(message.length() & 0xFF);
        
        socket.getOutputStream().write(bytes);
        
        byte[] messageBytes = new byte[message.length()];
        char[] messageChars = message.toCharArray();
        for (int i = 0; i < message.length(); i++)
        {
            messageBytes[i] = (byte)messageChars[i];
        }
        
        socket.getOutputStream().write(messageBytes);
        
        socket.close();
    }
}
