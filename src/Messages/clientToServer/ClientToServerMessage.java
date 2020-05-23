package Messages.clientToServer;



import java.io.Serializable;

public class ClientToServerMessage implements Serializable
{
    private String text;
    ClientToServerMessageType type;
    //CommunicatorType communicatorType;

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


    //public CommunicatorType getCommunicatorType(){ return communicatorType; };
}
