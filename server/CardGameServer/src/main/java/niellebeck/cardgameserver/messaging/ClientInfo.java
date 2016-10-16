package niellebeck.cardgameserver.messaging;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ClientInfo {
    public static final int MAX_MESSAGE_LENGTH = 1024 * 1024;
    
    public ClientState state;
    public ByteBuffer messageLengthBuffer;
    public ByteBuffer messageBuffer;
    public int messageLength;
    public SelectionKey key;
    
    public ClientInfo(SelectionKey key) {
        this.state = ClientState.IDLE;
        this.messageLengthBuffer = ByteBuffer.allocate(4);
        this.messageBuffer = ByteBuffer.allocate(MAX_MESSAGE_LENGTH);
        this.messageLength = -1;
        this.key = key;
    }
    
    public SocketAddress getAddress() throws IOException {
        return ((SocketChannel)key.channel()).getRemoteAddress();
    }
}
