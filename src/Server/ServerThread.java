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
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread{
    static Semaphore mutex;
    ServerSocket serverSocket;
    Socket clientSocket;
    ServerPrinterThread serverPrinterThread;
    ArrayList<User> connectedUsers;
    User userToHandle;
    boolean shouldRun = true;

    /* to send objects and receive */
    ObjectOutputStream outObject;
    ObjectInputStream inObject;

    static DatabaseHandler databaseHandler;


    ServerThread(ServerSocket serverSocket, Socket clientSocket, ServerPrinterThread serverPrinterThread, ArrayList<User> connectedUsers){
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
                    //System.out.println("Serwer: użytkownik zalogowany");
                    break;
                } else {
                    //System.out.println("Serwer: nie udało sie");
                    break;
                }
            }
        }catch( Exception e){
            e.printStackTrace();
        }
/* END OF LOG IN PHASE*/
        while(shouldRun){
            try {
                mutex.acquire();//========================
                processMessage( receiveMessage() );
                mutex.release();//========================
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


        System.out.println("End of thread");
    }

    /* throws exception if message is not REQUEST_LOGIN nor REQUEST_REGISTER
    *
    *   if operation is successful then add user do connectedUsers map
    *
    *   return true if login or register is successful
    * */
    boolean processLoginOrRegisterRequest() throws Exception{
        boolean answer=false;

        ClientToServerMessage message = (ClientToServerMessage)inObject.readObject();
        if( message.getType() != ClientToServerMessageType.REQUEST_LOGIN && message.getType() != ClientToServerMessageType.REQUEST_REGISTER){
            throw new Exception();
        }

        String[] loginAndPass = message.getString().split("#");
        //=========================================================================
        CommunicatorType communicatorType = message.getCommunicatorType();

        if( message.getType() == ClientToServerMessageType.REQUEST_LOGIN){
            for(User us: connectedUsers){
                if( us.getLogin().equals(loginAndPass[0]) ){ //user is already logged in
                    return answer;
                }
            }
            answer = databaseHandler.checkLogin(loginAndPass[0], loginAndPass[1]);
        }else{ //cannot be other that REQUEST_REGISTER since it was already checked
            answer = databaseHandler.registerUser(loginAndPass[0], loginAndPass[1]);
        }
        if( answer ){
            //==========================================================
            User user = new User(loginAndPass[0], clientSocket, communicatorType);
            connectedUsers.add(user);
            userToHandle = user;

            /*for(User us: connectedUsers){
                System.out.println( us.getLogin());
            }*/
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

    /*
    * returns message received by inObject received
    * */
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

    /*
    * Removes user from connectedUsers and ends thread
    * */
    void logoutUser(){
        connectedUsers.remove(userToHandle);
        userToHandle=null;
        shouldRun = false;
        /*for(User us: connectedUsers){
            System.out.println( us.getLogin());
        }*/
    }



    /*
    * Behaves like multiplexer for messages
    * */
    void processMessage( ClientToServerMessage message ){
        if( message == null){
            return;
        }
        ClientToServerMessageType type = message.getType();
        String text = message.getText();

        try {
            switch (type) {
                case LOGOUT:
                    logoutUser();
                    break;
                default:
                    throw new Exception();
            }
        }catch (Exception e){
            System.out.println("ZŁA WIADOMOŚĆ");

        }
    }

}
