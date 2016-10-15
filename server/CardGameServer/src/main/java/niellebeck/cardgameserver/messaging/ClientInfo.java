package niellebeck.cardgameserver.messaging;

import java.nio.ByteBuffer;

public class ClientInfo {
    public static final int MAX_MESSAGE_LENGTH = 1024 * 1024;
    
    public ClientState state;
    public ByteBuffer messageLengthBuffer;
    public ByteBuffer messageBuffer;
    public int messageLength;
    
    public ClientInfo() {
        state = ClientState.IDLE;
        messageLengthBuffer = ByteBuffer.allocate(4);
        messageBuffer = ByteBuffer.allocate(MAX_MESSAGE_LENGTH);
        messageLength = -1;
    }
}
