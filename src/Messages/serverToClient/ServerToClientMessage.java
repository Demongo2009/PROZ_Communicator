package Messages.serverToClient;

import Messages.Message;

public class ServerToClientMessage extends Message {
    ServerToClientMessageType type;

    public ServerToClientMessage(ServerToClientMessageType type, String text){
        this.type = type;
        this.text = text;
    }

    public ServerToClientMessageType getType(){ return type; }
}
