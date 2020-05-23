package Messages.clientToServer;



import java.io.Serializable;

public class ClientToServerMessage implements Serializable
{
    private String text;
    private ClientToServerMessageType type;

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
