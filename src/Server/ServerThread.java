package Server;

import Messages.Message;
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

    ArrayList<User> connectedUsers;
    User userToHandle;
    boolean shouldRun = true;

    /* to send objects and receive */
    ObjectOutputStream outObject;
    ObjectInputStream inObject;

    static DatabaseHandler databaseHandler;


    ServerThread(ServerSocket serverSocket, Socket clientSocket, ArrayList<User> connectedUsers){
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.connectedUsers = connectedUsers;
        mutex = new Semaphore(1);

        try {
            outObject = new ObjectOutputStream( clientSocket.getOutputStream() );
            inObject = new ObjectInputStream( clientSocket.getInputStream() );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {
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

    /*  throws exception if message is not REQUEST_LOGIN nor REQUEST_REGISTER
    *
    *   if operation is successful then add user to connectedUsers map
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
            User user = new User(loginAndPass[0], clientSocket, communicatorType, outObject);
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
/*============================================================*/
    User getUserFromConnectedUsers(String login){
        for(User user: connectedUsers){
            if( user.getLogin().equals(login)){
                return user;
            }
        }
        return null;//no user found
    }


    /* don't know that this function if for for now*/
    ServerToClientMessage prepareMessage(ServerToClientMessageType type, String text){
        ServerToClientMessage message = new ServerToClientMessage( type, text);
        return message;
    }

    /* returns true if message is being sent*/
    boolean sendMessage(ServerToClientMessage message, User userToSend){

        try {
            ObjectOutputStream tmpOut = userToSend.getObjectOutputStream();
            tmpOut.writeObject(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
/*====================================================================*/
    /*
    * returns message received by inObject received
    * */
    ClientToServerMessage receiveMessage(){
        ClientToServerMessage message= null;

        try {
            message = (ClientToServerMessage) inObject.readObject();

        } catch (Exception e) {
            shouldRun = false; /* DON'T KNOW YET*/
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
    }

    void processTextMessage(String textMessage){
        String userAndText[] = textMessage.split("#");
        System.out.println("User: " + userAndText[0]);
        System.out.println("Text: " + userAndText[1]);


        /*TODO: check if user is connected, if not then say it to sender
            if yes then send it to this user with a special Message
           */
    }

    void processAddUserToFriends(String loginToAdd){
        //System.out.println(userToAdd);
        /*TODO: check if user is connected, if not || do nothing OR communicate it
         *  if yes then send request to this user*/

        User user = getUserFromConnectedUsers(loginToAdd);
        if( user == null ){
            /* Do nothing or communicate it to sender, don't know yet*/
            System.out.println("No user found");
        }
        String text = userToHandle.getLogin();
        ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND, text);
        sendMessage(message, user);

    }

    void processConfirmationOfFriendship(String newFriend){
        /*TODO:
            databaseHandler.insertFriends(userToHandle.getLogin(), newFriend);
            ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.CONFIRMATION_OF_FRIENDSHIP, userToHandle.getLogin() );
            User user = getUserFromConnectedUsers( newFriend );
            if( user == null ){
                //do nothing since the friendship is already booked in database
                return;
            }
            sendMessage( newMessage, user );

         */
    }

    /*
    * Behaves like multiplexer for messages types
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
                case ADD_USER_TO_FRIENDS:
                    processAddUserToFriends(text);
                    break;
                case TEXT:
                    processTextMessage(text);
                    break;
                default:
                    throw new Exception("Invalid message from client received");
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

}
