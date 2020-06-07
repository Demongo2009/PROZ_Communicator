package Server;

import java.io.*;
import java.net.Socket;


/**
 * Class to handle users on server*/
public class User{
    private String login;
    private Socket userSocket;
    private ObjectOutputStream outObject;


    public User(String login, Socket socket, ObjectOutputStream outObject){
        this.login = login;
        this.userSocket = socket;
        this.outObject = outObject;
    }

    String getLogin(){ return login; }
    Socket getSocket(){ return userSocket; }
    ObjectOutputStream getObjectOutputStream(){ return outObject; }
}
