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



/*
public class User {

    public User(String name, int hashedAddress, int port) {
        this.name = name;
        this.hashedAddress = hashedAddress;
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHashedAddress() {
        return hashedAddress;
    }

    public void setHashedAddress(int hashedAddress) {
        this.hashedAddress = hashedAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void addToBuffer(String text){
        buffer+=text+"\n";
    }

    public boolean trySend(){
        Socket partnerSocket = null;
        for(Socket s: Server.clientSocketArray){
                    if( (s.getPort() == port) && (s.getInetAddress().hashCode() == hashedAddress) ){
                        partnerSocket = s;
                    }
        }
        if(partnerSocket == null){
            return false;
        }

        try {
            PrintWriter partnerOut = new PrintWriter(partnerSocket.getOutputStream(), true);
            partnerOut.println(buffer);
            buffer = "";
        } catch (IOException e) {
                    e.printStackTrace();
        }

        return true;
    }

    String buffer = "";
    String name;
    int hashedAddress;
    int port;


}
*/