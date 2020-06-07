package Messages.serverToClient;

import java.io.Serializable;


/**
 * Class to handle communication from Server to Client
 * */
public class ServerToClientMessage implements Serializable {
    private ServerToClientMessageType type;
    private String text;
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
