package Messages.serverToClient;



import java.io.Serializable;

public class ServerToClientMessage implements Serializable {
    ServerToClientMessageType type;
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
