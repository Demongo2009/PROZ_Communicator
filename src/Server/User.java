package Server;




import java.io.*;
import java.net.Socket;
import java.nio.Buffer;
import java.util.stream.Stream;

public class User{
    private String login;
    private Socket userSocket;
    private CommunicatorType communicatorType;
    private ObjectOutputStream outObject;
    //


    public User(String login, Socket socket, CommunicatorType communicatorType, ObjectOutputStream outObject){
        this.login = login;
        this.userSocket = socket;
        this.communicatorType = communicatorType;
        this.outObject = outObject;
    }

    String getLogin(){ return login; }
    Socket getSocket(){ return userSocket; }
    CommunicatorType getCommunicatorType(){ return communicatorType; }
    ObjectOutputStream getObjectOutputStream(){ return outObject; }
}
