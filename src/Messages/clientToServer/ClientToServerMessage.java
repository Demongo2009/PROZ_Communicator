package Messages.clientToServer;

import Messages.Message;

public class ClientToServerMessage extends Message {
    ClientToServerMessageType type;

    public ClientToServerMessage( ClientToServerMessageType type, String text ){
        this.type = type;
        this.text = text;
    }

    public ClientToServerMessageType getType(){ return type; }
}
