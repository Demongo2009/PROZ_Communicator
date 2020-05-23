package Server;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread{
    private static Semaphore mutex;
    private Socket clientSocket;

    private ArrayList<User> connectedUsers;
    private User userToHandle;
    private boolean shouldRun = true;
    private boolean isLogged = false;

    /* to send objects and receive */
    private ObjectOutputStream outObject;
    private ObjectInputStream inObject;

    private static DatabaseHandler databaseHandler;

    private ArrayList<Group> groups;


    ServerThread( Socket clientSocket, ArrayList<User> connectedUsers, ArrayList<Group> groups){
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
        while(!isLogged)
        {
            try {

                if (sendLoginAnswer(processLoginOrRegisterRequest())) {
                    //System.out.println("Serwer: użytkownik zalogowany");
                    isLogged=true;

                } else {
                    //System.out.println("Serwer: nie udało sie");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        //System.out.println("End of thread");
        try {
            inObject.close();
            outObject.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * throws exception if message is not REQUEST_LOGIN nor REQUEST_REGISTER
     *
     *   if operation is successful then add user to connectedUsers map
     *
     *   return true if login or register is successful
     * */
    private boolean processLoginOrRegisterRequest() throws Exception{
        boolean answer=false;

        ClientToServerMessage message = (ClientToServerMessage)inObject.readObject();
        if( message.getType() != ClientToServerMessageType.REQUEST_LOGIN && message.getType() != ClientToServerMessageType.REQUEST_REGISTER){
            throw new Exception();
        }

        String[] loginAndPass = message.getText().split("#");

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
            User user = new User(loginAndPass[0], clientSocket, outObject);
            connectedUsers.add(user);
            userToHandle = user;
        }

        return answer;
    }



    /**
     *   sends ServerToClientMessage with adequate MessageType and text
     *
     *    returns true if answer is positive
     * */
    private boolean sendLoginAnswer(boolean answer){
        ServerToClientMessageType type = null;
        String text = "";
        if( answer ){
            type = ServerToClientMessageType.CONFIRM_LOGIN;
            text +="#";
            text += databaseHandler.getUserFriends( userToHandle.getLogin() ); //send to user his friends
            text += "@#"; //in order to not split empty array
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

    /**
     * Returns User object based on given login
     * if user is not found then returns null
     * */
    private User getUserFromConnectedUsers(String login){
        for(User user: connectedUsers){
            if( user.getLogin().equals(login)){
                return user;
            }
        }
        return null;//no user found
    }


    /**
     * returns true if message is being sent
     * */
    private boolean sendMessage(ServerToClientMessage message, User userToSend){

        try {
            ObjectOutputStream tmpOut = userToSend.getObjectOutputStream();
            tmpOut.writeObject(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * returns message received by inObject received
     * if exception is thrown then end the thread
     * */
    private ClientToServerMessage receiveMessage(){
        ClientToServerMessage message= null;

        try {
            message = (ClientToServerMessage) inObject.readObject();

        } catch (Exception e) {
            shouldRun = false;
            e.printStackTrace();
        }
        return message;
    }

    /**
     * Removes user from connectedUsers
     * Sends to client listener communicate to end the listener thread
     * ends thread
     * */
    private void logoutUser(){
        /* send to user listener thread that he can stop listening*/
        ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.LOGOUT, "");
        sendMessage(message, userToHandle);

        connectedUsers.remove(userToHandle);
        userToHandle=null;
        shouldRun = false;
        isLogged=false;
    }

    /**
     * sends text message to user
     * if user is not connected, it sends the message to sender that communicates it
     * */
    private void processTextMessage(String textMessage){
        if( textMessage == null ){
            return; // should never occur
        }
        String userAndText[] = textMessage.split("#");

        System.out.println(userAndText[0]);
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
    /**
     * sends to user that userToHandle wants to add to friends a USER_WANT_TO_BE_YOUR_FRIEND message
     * if that user does not exit then does nothing
     * if users are already friend then does nothing
     * */
    private void processAddUserToFriends(String loginToAdd){
        if( databaseHandler.checkFriendship( userToHandle.getLogin(), loginToAdd) ){
            //System.out.println("USERS ARE FRIENDS ALREADY");
            return;//users are friends already, no reason to send it further, should never occur since client checks it
        }

        User user = getUserFromConnectedUsers(loginToAdd);
        if( user == null ){
            /* Do nothing or communicate it to sender, don't know yet*/
            //System.out.println("No user found");
            return;
        }
        String text = userToHandle.getLogin();
        ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND, text);
        sendMessage(message, user);

    }

    /**
     * registers both users as friends in database
     * and communicates it to first_user
     * */
    private void processConfirmationOfFriendship(String newFriend){
        if( !databaseHandler.checkFriendship( userToHandle.getLogin(), newFriend)) { // it is possible to send few requests and to confirm these few requests, so we check if friendship is not already booked
            databaseHandler.insertFriendship(newFriend, userToHandle.getLogin());
            User user = getUserFromConnectedUsers( newFriend );
            if( user == null ){
                //do nothing since the friendship is already booked in database and user will get this friendship when he log in
                return;
            }
            ServerToClientMessage message = new ServerToClientMessage( ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST, userToHandle.getLogin());
            sendMessage( message, user );
        }
    }

    /**
     * returns group_names, in which given user is, separated by '#'
     * */
    private String getUserGroups(String user){
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

    /**
     * create group if group_name is not occupied
     * if it is occpied then communicate it to user
     * */
    private void processCreateGroup(String groupName){
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

    /**
     *
    * */
    private void processTextGroupMessage(String text){
        String[] groupAndUserAndText = text.split("#");
        Group group=null;
        for( Group g: groups){
            if( g.getGroupName().equals(groupAndUserAndText[0])){
                group = g;
                break;
            }
        }
        if( group == null){//should never occur
            //System.out.println("GROUP DOES NOT EXIST");
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
    /**
     * Adds user to groups
     * Argument should look like
     * "GROUP_NAME # USER"
     * */
    private void processAddUserToGroup(String text){
        String[] groupAndUser = text.split("#");

        Group group = null;
        for( Group g: groups){
            if( g.getGroupName().equals(groupAndUser[0])){
                group = g;
                break;
            }
        }
        if(group == null){
            //System.out.println("Group does not exist");
            return;
        }
        if( group.getSize() >= 4){
            //System.out.println("Group is full");
            return;
        }
        if( !databaseHandler.checkIfUserExists( groupAndUser[1])){
            //System.out.println("User does not exists");
            return;
        }
        for(int i=0; i<group.getSize(); ++i){
            if( group.getUser(i).equals(groupAndUser[1])){
                //System.out.println("User is already in group");
                return;
            }
        }

        databaseHandler.addUserToGroup(groupAndUser[0], groupAndUser[1]);
        group.addUser(groupAndUser[1]);
        User user = getUserFromConnectedUsers(groupAndUser[1]);
        ServerToClientMessageType type = ServerToClientMessageType.USER_ADDED_YOU_TO_GROUP;
        ServerToClientMessage message = new ServerToClientMessage(type, groupAndUser[0]);
        sendMessage( message, user);

    }


    /**
     * Behaves like multiplexer for messages types
     * calls functions depending on message received
     * */
    private void processMessage( ClientToServerMessage message ){
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