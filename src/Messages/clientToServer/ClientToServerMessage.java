package Messages.clientToServer;

import Messages.Message;
import Server.CommunicatorType;

import java.io.Serializable;

public class ClientToServerMessage implements Serializable
{
    private String text;
    ClientToServerMessageType type;
    //CommunicatorType communicatorType;
    public ClientToServerMessage(ClientToServerMessageType type, String text, CommunicatorType communicatorType){
        this.type = type;
        this.text = text;
        //this.communicatorType = communicatorType;
    }
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
