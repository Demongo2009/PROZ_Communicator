package Server;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
//import Server.Protocol;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import Server.ServerPrinterThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread{
    static Semaphore mutex;
    ServerSocket serverSocket;
    Socket clientSocket;
    ServerPrinterThread serverPrinterThread;
    Map<String, Socket> connectedUsers;
    boolean shouldRun = true;

    /* to send obejects */
    static ObjectOutputStream outObject;
    /* to receive objects */
    static ObjectInputStream inObject;
    DatabaseHandler databaseHandler;

    ServerThread(ServerSocket serverSocket, Socket clientSocket, ServerPrinterThread serverPrinterThread, Map<String, Socket> connectedUsers){
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.serverPrinterThread = serverPrinterThread;
        this.connectedUsers = connectedUsers;
        mutex = new Semaphore(1);

        try {
            outObject = new ObjectOutputStream( clientSocket.getOutputStream() );
            inObject = new ObjectInputStream(( clientSocket.getInputStream()) );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {

        PrintWriter out = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));


        } catch (IOException e) {
            e.printStackTrace();
        }
        databaseHandler = new DatabaseHandler();
/* LOG IN PHASE*/
        try {
            while (true) {
                if (sendLoginAnswer(processLoginOrRegisterRequest())) {
                    System.out.println("Serwer: użytkownik zalogowany");
                    /* dodac uzytkownika do tablicy uzytkownikow zalogowanych*/
                    break;
                } else {
                    System.out.println("Serwe: nie udało sie");
                    break;
                }
            }
        }catch( Exception e){
            e.printStackTrace();
        }
/* END OF LOG IN PHASE*/
        while(shouldRun){
            processMessage( receiveMessage() );
        }
        System.out.println("Skończyłem wątek");
    }

/* throws exception if message is not REQUEST_LOGIN nor REQUEST_REGISTER
*
*   if operation is succesfull then add user do connectedUsers map
*
*   return true if login or register is succesful
* */
    boolean processLoginOrRegisterRequest() throws Exception{
        boolean answer;

        ClientToServerMessage message = (ClientToServerMessage)inObject.readObject();
        if( message.getType() != ClientToServerMessageType.REQUEST_LOGIN
                && message.getType() != ClientToServerMessageType.REQUEST_REGISTER){
            throw new Exception();
        }

        String[] loginAndPass = message.getString().split("#");

        if( message.getType() == ClientToServerMessageType.REQUEST_LOGIN){
            answer = databaseHandler.checkLogin(loginAndPass[0], loginAndPass[1]);
        }else{
            answer = databaseHandler.registerUser(loginAndPass[0], loginAndPass[1]);
        }
        if( answer ){
            connectedUsers.put(loginAndPass[0], clientSocket);
        }

        return answer;
    }

/*
*   sends ServerToClientMessage with adequate MessageType and text
*
*    returns true if answer is positive
* */
    boolean sendLoginAnswer(boolean answer){
        ServerToClientMessageType type = null;
        String text = null;
        if( answer ){
            type = ServerToClientMessageType.CONFIRM_LOGIN;
        }else{
            type = ServerToClientMessageType.REJECT_LOGIN;
            text = "Invalid login";
        }

        ServerToClientMessage message = new ServerToClientMessage(type, "");

        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }

    ClientToServerMessage receiveMessage(){
        ClientToServerMessage message= null;

        try {
            message = (ClientToServerMessage) inObject.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return message;
    }

    void logoutUser(String login){
        connectedUsers.remove(login);
        System.out.println("Kończę wątek");
        shouldRun = false;
    }


    void processMessage( ClientToServerMessage message ){
        ClientToServerMessageType type = message.getType();
        String text = message.getText();

        try {
            switch (type) {
                case LOGOUT:
                    logoutUser(text);
                    break;
                default:
                    throw new Exception();
            }
        }catch (Exception e){
            System.out.println("ZŁA WIADOMOŚĆ");

        }
    }
}
