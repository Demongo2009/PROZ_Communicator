package Messages.clientToServer;

import Messages.Message;

public class ClientToServerMessage extends Message {
    ClientToServerMessageType type;

    public ClientToServerMessage( ClientToServerMessageType type, String text ){
        this.type = type;
        this.text = text;
    }
    public ClientToServerMessage( ClientToServerMessageType type){
        this.type = type;
        this.text = "";
    }

    public ClientToServerMessageType getType(){ return type; }
    public String getText() { return text; }
}
