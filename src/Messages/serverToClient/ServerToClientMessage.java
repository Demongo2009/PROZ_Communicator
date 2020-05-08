package Messages.serverToClient;

import Messages.Message;
import Server.CommunicatorType;

public class ServerToClientMessage extends Message {
    ServerToClientMessageType type;

    public ServerToClientMessage(ServerToClientMessageType type, String text){
        this.type = type;
        this.text = text;
    }
    public ServerToClientMessage(ServerToClientMessageType type){
        this.type = type;
        this.text = "";
    }

    public ServerToClientMessageType getType(){ return type; }
    public String getText() {return text;}
}
