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

    ArrayList<Group> groups;


    ServerThread(ServerSocket serverSocket, Socket clientSocket, ArrayList<User> connectedUsers, ArrayList<Group> groups){
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.connectedUsers = connectedUsers;
        this.groups = groups;
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
            if (sendLoginAnswer(processLoginOrRegisterRequest())) {
                System.out.println("Serwer: użytkownik zalogowany");
            } else {
                System.out.println("Serwer: nie udało sie");
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
        String text = "";
        if( answer ){
            type = ServerToClientMessageType.CONFIRM_LOGIN;
            text += databaseHandler.getUserFriends( userToHandle.getLogin() ); //send to user his friends
            text += "#@#"; //in order to not split empty array
            text += getUserGroups(userToHandle.getLogin());
        }else{
            type = ServerToClientMessageType.REJECT_LOGIN;
        }

        ServerToClientMessage message = new ServerToClientMessage(type, text);

        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }

    /*
     * Returns User object based on given login
     * */
    User getUserFromConnectedUsers(String login){
        for(User user: connectedUsers){
            if( user.getLogin().equals(login)){
                return user;
            }
        }
        return null;//no user found
    }


    /* don't know what this function is for for now*/
    ServerToClientMessage prepareMessage(ServerToClientMessageType type, String text){
        ServerToClientMessage message = new ServerToClientMessage( type, text);
        return message;
    }

    /*
     * returns true if message is being sent
     * */
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
        /* send to user listener thread that he can stop listening*/
        ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.LOGOUT, "");
        sendMessage(message, userToHandle);

        connectedUsers.remove(userToHandle);
        userToHandle=null;
        shouldRun = false;
    }

    /*
     * sends text message to user
     * if user is not connected it sends the message to sender that communicates it
     * */
    void processTextMessage(String textMessage){
        if( textMessage == null ){
            return; // should never occur
        }
        String userAndText[] = textMessage.split("#");

        if( !databaseHandler.checkFriendship(userToHandle.getLogin(), userAndText[0]) ){
            System.out.println("USERS ARE NOT FRIENDS - something went wrong, client should check it");
            return;
        }

        User user = getUserFromConnectedUsers(userAndText[0]);
        if( user == null ){
            //communicate to sender that user is not connected
            ServerToClientMessageType type = ServerToClientMessageType.USER_IS_NOT_CONNECTED;
            ServerToClientMessage message = new ServerToClientMessage(type, userAndText[0]); /* we communicate to whom we couldn't send the message */
            sendMessage(message, userToHandle);
            return;
        }
        ServerToClientMessageType type = ServerToClientMessageType.TEXT_MESSAGE_FROM_USER;
        String text = userToHandle.getLogin() + "#" + userAndText[1];
        ServerToClientMessage message = new ServerToClientMessage(type, text);
        sendMessage( message, user);
    }
    /*
     * sends to user that userToHandle wants to add to friends a USER_WANT_TO_BE_YOUR_FRIEND message
     * if that user does not exit =============================
     * if users are already friend then does nothing
     * */
    void processAddUserToFriends(String loginToAdd){
        //System.out.println(userToAdd);
        /*TODO: check if user is connected, if not || do nothing OR communicate it
         *  if yes then send request to this user*/

        if( databaseHandler.checkFriendship( userToHandle.getLogin(), loginToAdd) ){
            System.out.println("USERS ARE FRIENDS ALREADY");
            return;//users are friends already, no reason to send it further, should never occur since client checks it
        }

        User user = getUserFromConnectedUsers(loginToAdd);
        if( user == null ){
            /* Do nothing or communicate it to sender, don't know yet*/
            System.out.println("No user found");
            return;
        }
        String text = userToHandle.getLogin();
        ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND, text);
        sendMessage(message, user);

    }

    /*
     * registers both users as friends in database
     * and communicates it to first_user
     * */
    void processConfirmationOfFriendship(String newFriend){
        if( !databaseHandler.checkFriendship( userToHandle.getLogin(), newFriend)) { // it is possible to send few requests and to confirm these few requests, so we check if friendship is not already booked
            databaseHandler.insertFriendship(newFriend, userToHandle.getLogin());
            User user = getUserFromConnectedUsers( newFriend );
            if( user == null ){
                //do nothing since the friendship is already booked in database and user will get this friendship when he log in
                return;
            }
            ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST, newFriend);
            sendMessage( message, user );
        }
    }

    /*
     * returns group_names, in which given user is, separated by '#'
     * */
    String getUserGroups(String user){
        String userGroups = "";
        for(Group group: groups){
            for(int i=0; i<group.getSize(); ++i){
                if( group.getUser(i).equals(user)){
                    userGroups += group.getGroupName() + "#";
                    break;
                }
            }
        }
        return userGroups;
    }

    /*
     * create group if group_name is not occupied
     * if it is occpied then communicate it to user
     * */
    void processCreateGroup(String groupName){
        ServerToClientMessage message;
        if( databaseHandler.checkIfGroupExists(groupName)){
            ServerToClientMessageType type = ServerToClientMessageType.GROUP_NAME_OCCUPIED;
            message = new ServerToClientMessage(type, groupName);
        }else{
            Group newGroup = new Group(groupName);
            newGroup.addUser(userToHandle.getLogin());
            groups.add(newGroup);
            databaseHandler.createGroup(newGroup);

            ServerToClientMessageType type = ServerToClientMessageType.USER_ADDED_YOU_TO_GROUP;
            message = new ServerToClientMessage(type, groupName);
        }

        sendMessage( message, userToHandle);
    }


    //TODO:
    void processTextGroupMessage(String text){
        String[] groupAndUserAndText = text.split("#");
        Group group=null;
        for( Group g: groups){
            if( g.getGroupName().equals(groupAndUserAndText[0])){
                group = g;
                break;
            }
        }
        if( group == null){//should never occur
            System.out.println("GROUP DOES NOT EXIST");
            return;
        }

        for(int i=0; i<group.getSize(); ++i){
            String login = group.getUser(i);
            User user = getUserFromConnectedUsers(login);
            if( user == null ){
                continue;//user is not connected
            }
            if( userToHandle == user){
                continue;//don't send to message to yourself
            }

            ServerToClientMessageType type = ServerToClientMessageType.TEXT_MESSAGE_FROM_GROUP;
            ServerToClientMessage message = new ServerToClientMessage(type, text);
            sendMessage(message, user);
        }
    }

    void processAddUserToGroup(String text){
        String[] groupAndUser = text.split("#");

        Group group = null;
        for( Group g: groups){
            if( g.getGroupName().equals(groupAndUser[0])){
                group = g;
                break;
            }
        }
        if(group == null){
            System.out.println("Group does not exist");
            return;
        }
        if( group.getSize() >= 4){
            System.out.println("Group is full");
            return;
        }
        if( !databaseHandler.checkIfUserExists( groupAndUser[1])){
            System.out.println("User does not exists");
        }
        for(int i=0; i<group.getSize(); ++i){
            if( group.getUser(i).equals(groupAndUser[1])){
                System.out.println("User is already in group");
                return;
            }
        }

        //TODO: send to user that he is added to group
        databaseHandler.addUserToGroup(groupAndUser[0], groupAndUser[1]);
        group.addUser(groupAndUser[1]);

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

        //=============
        //System.out.println(text);
        ////===========

        try {
            switch (type) {
                case LOGOUT:
                    logoutUser();
                    break;
                case ADD_USER_TO_FRIENDS:
                    processAddUserToFriends(text);
                    break;
                case CONFIRMATION_OF_FRIENDSHIP:
                    processConfirmationOfFriendship(text);
                    break;
                case TEXT_TO_USER:
                    processTextMessage(text);
                    break;
                case CREATE_GROUP:
                    processCreateGroup(text);
                    break;
                case ADD_USER_TO_GROUP:
                    processAddUserToGroup(text);
                    break;
                case TEXT_TO_GROUP:
                    processTextGroupMessage(text);
                    break;
                default:
                    throw new Exception("Invalid message from client received");
            }
        }catch (Exception e){
            e.printStackTrace();

        }
    }

}