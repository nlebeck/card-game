package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;

public class TcpMessager {
    private static ServerSocketChannel serverSocketChannel = null;
    private static SelectionKey serverSocketSelectionKey;
    private static Selector selector = null;
    
    private static ClientInfoContainer clientInfoContainer;
    
    public static void init(int listeningPort) throws IOException {
        clientInfoContainer = new ClientInfoContainer();
        
        selector = Selector.open();
        
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(listeningPort));
        serverSocketChannel.configureBlocking(false);
        
        serverSocketSelectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }
    
    public static void startMessageLoop(MessageCallback callback) {
        while(true) {
            try {
                selector.select();
            } catch (IOException e) {
                logError(e, "Error calling select()");
            }
            for (SelectionKey key : selector.selectedKeys()) {
                if (key.equals(serverSocketSelectionKey)) {
                    SocketChannel socketChannel = null;
                    SelectionKey clientKey = null;
                    SocketAddress clientAddress = null;
                    
                    try {
                        socketChannel = serverSocketChannel.accept();
                        socketChannel.configureBlocking(false);
                        clientKey = socketChannel.register(selector, SelectionKey.OP_READ);
                        clientAddress = socketChannel.getRemoteAddress();
                    }
                    catch (IOException e) {
                        logError(e, "Error accepting client connection");
                    }
                    
                    ClientInfo clientInfo = new ClientInfo(clientKey, clientAddress);
                    if (!clientInfoContainer.containsSocketAddress(clientInfo.address)) {
                        clientInfoContainer.add(clientInfo);
                    }
                }
                else if (clientInfoContainer.containsSelectionKey(key)) {
                    readClientData(clientInfoContainer.get(key), callback);
                }
            }
            selector.selectedKeys().clear();
        }

    }
    
    private static void logError(Exception e, String message) {
        System.err.println(message + ": " + e.toString());
    }
    
    private static void handleDisconnectedClient(ClientInfo clientInfo) {
        clientInfo.key.cancel();
        clientInfoContainer.remove(clientInfo);
    }
    
    private static void readClientData(ClientInfo clientInfo, MessageCallback callback) {
        SocketChannel socketChannel = (SocketChannel)clientInfo.key.channel();
        
        if (clientInfo.state == ClientState.IDLE) {
            clientInfo.state = ClientState.SENDING_LENGTH;
        }
        
        if (clientInfo.state == ClientState.SENDING_LENGTH) {
            try {
                socketChannel.read(clientInfo.messageLengthBuffer);
            }
            catch (IOException e) {
                handleDisconnectedClient(clientInfo);
            }
            
            if (clientInfo.messageLengthBuffer.position() == 4) {
                clientInfo.messageLengthBuffer.flip();
                byte[] messageLengthBytes = new byte[4];
                clientInfo.messageLengthBuffer.get(messageLengthBytes);
                
                int messageLength = (messageLengthBytes[0] << 24) | (messageLengthBytes[1] << 16) | (messageLengthBytes[2] << 8) | messageLengthBytes[3];
                if (messageLength > ClientInfo.MAX_MESSAGE_LENGTH) {
                    messageLength = ClientInfo.MAX_MESSAGE_LENGTH;
                }
                
                clientInfo.messageLength = messageLength;
                clientInfo.state = ClientState.SENDING_MESSAGE;
            }
        }
        
        if (clientInfo.state == ClientState.SENDING_MESSAGE) {
            try {
                socketChannel.read(clientInfo.messageBuffer);
            }
            catch (IOException e) {
                handleDisconnectedClient(clientInfo);
            }
            
            if (clientInfo.messageBuffer.position() == clientInfo.messageLength) {
                clientInfo.messageBuffer.flip();
                byte[] messageBytes = new byte[clientInfo.messageLength];
                clientInfo.messageBuffer.get(messageBytes);
                
                char[] messageChars = new char[messageBytes.length];
                for (int i = 0; i < messageBytes.length; i++) {
                    messageChars[i] = (char)messageBytes[i];
                }
                String message = String.valueOf(messageChars, 0, messageChars.length);
                
                SocketAddress socketAddress = null;
                try {
                    socketAddress = socketChannel.getRemoteAddress();
                }
                catch (IOException e) {
                    logError(e, "Error getting remote address of client");
                }
                
                callback.callback(socketAddress, message);
                
                clientInfo.state = ClientState.IDLE;
                clientInfo.messageLengthBuffer.clear();
                clientInfo.messageBuffer.clear();
                clientInfo.messageLength = -1;
            }
        }
    }
    
    public static void sendMessage(SocketAddress address, String message) {
        ClientInfo clientInfo = clientInfoContainer.get(address);
        SocketChannel socketChannel = (SocketChannel)clientInfo.key.channel();
        
        byte[] messageLengthBytes = new byte[4];
        messageLengthBytes[0] = (byte)((message.length() >> 24) & 0xFF);
        messageLengthBytes[1] = (byte)((message.length() >> 16) & 0xFF);
        messageLengthBytes[2] = (byte)((message.length() >> 8) & 0xFF);
        messageLengthBytes[3] = (byte)(message.length() & 0xFF);
        
        ByteBuffer messageLengthBuffer = ByteBuffer.allocate(messageLengthBytes.length);
        messageLengthBuffer.put(messageLengthBytes);
        messageLengthBuffer.flip();
        
        try {
            socketChannel.write(messageLengthBuffer);
        }
        catch (IOException e) {
            handleDisconnectedClient(clientInfo);
        }
        
        byte[] messageBytes = new byte[message.length()];
        char[] messageChars = message.toCharArray();
        for (int i = 0; i < message.length(); i++)
        {
            messageBytes[i] = (byte)messageChars[i];
        }
        
        ByteBuffer messageBuffer = ByteBuffer.allocate(messageBytes.length);
        messageBuffer.put(messageBytes);
        messageBuffer.flip();
        
        try {
            socketChannel.write(messageBuffer);
        }
        catch (IOException e) {
            handleDisconnectedClient(clientInfo);
        }
    }
}
