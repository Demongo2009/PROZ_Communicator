package Client;

import Client.GUI.StartingScreen.StartingScreen;
import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;


import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import static Client.GUI.tools.SwingConsole.run;

/**
 * Class that implements logic of the client
 * */
public class Client {

    private static String hostName = "localhost";
    private static int serverPort = 9999;

    private static Socket echoSocket;

    /* to send and receive objects */
    private static ObjectOutputStream outObject;
    private static ObjectInputStream inObject;

    private static String username = null;
    private static ClientPrinterThread listener;

    public static ArrayList<String> friends;
    public static ArrayList<String> groups;


    public static NotificationsHandler notificationsHandler;


    public static void main(String[] args) {
        initClient();
        run(new StartingScreen(),300,140);

    }
    /**
     * initializes Sockets, ObjectStreams, fiends and groups
     * */
    public static void initClient(){
        try {
            echoSocket = new Socket(hostName, serverPort);
            // shutdown hook added for closing the connection if client exits
            // Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));

            outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
            inObject = new ObjectInputStream( echoSocket.getInputStream() );

            friends = new ArrayList<>();
            groups = new ArrayList<>();
            notificationsHandler = new NotificationsHandler();




        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * @param login nickname
     * @param password password
     * @param type LOGIN_REQUEST or REGISTER_REQUEST
     * Sends to server LOGIN_REQUEST or REGISTER_REQUEST(decided by argument) with login and password.
     * @throws Exception if type is none of above OR login or password contain '#' OR they are shorter than 4 characters
     * */
    public static void sendLoginOrRegisterRequest(String login, String password, ClientToServerMessageType type) throws Exception{
        if( type != ClientToServerMessageType.REQUEST_LOGIN && type != ClientToServerMessageType.REQUEST_REGISTER){
            throw new Exception("Only REQUEST_LOGIN or REQUEST_REGISTER");
        }
        if(login.contains("#") || password.contains("#")){
            throw new Exception("Cannot use '#'");
        }
        if( login.length() < 3 || password.length() < 3){
            throw new Exception("Login and password must have at least 3 characters");
        }
        if( login.contains(" ") || password.contains(" ")){
            throw new Exception("Login and password must be one word");
        }

        username=login;
        String textToSend = login + "#" + password;
        ClientToServerMessage message = new ClientToServerMessage(type, textToSend );
        sendMessage( message );
    }

    /**
     * starts listener thread
     * @throws Exception if received message is not CONFIRM nor REJECT
     * @return true if answer is positive, false otherwise
     * */
    public static boolean receiveLoginAnswer() throws Exception{
        ServerToClientMessage message = null;
        try {
            message = (ServerToClientMessage)inObject.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerToClientMessageType response = message.getType();
        if( response == ServerToClientMessageType.REJECT_LOGIN )
        {
            return false;
        }
        else if( response == ServerToClientMessageType.CONFIRM_LOGIN )
        {
            //get friends and groups from server
            String[] friendsAndGroups = message.getText().split("@");
            String[] friendsArray = friendsAndGroups[0].split("#");
            String[] groupsArray = friendsAndGroups[1].split("#");

            friends.addAll(Arrays.asList(friendsArray));//inserts all strings into array list
            groups.addAll(Arrays.asList(groupsArray));

            /*Start of listener thread*/
            listener = new ClientPrinterThread(inObject/*, friends*/);
            listener.start();
            return true;
        }
        else
        {
            throw new Exception(" NOT CONFIRM NOR REJECTION");
        }

    }



    /**
     * @param message message
     * Sends message to server via ObjectOutputStream
     * */
    private static void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param userToAdd who we want to be friends with
     * @return true if message is sent
     * checks if we are not adding ourself to friends and if we are not adding user that is out friend already
     * */
    public static boolean addUserToFriends(String userToAdd){
        if( checkFriendship(userToAdd) ){
            return false;
        }
        if( userToAdd.equals(username)) {
            return false;
        }

        ClientToServerMessageType type = ClientToServerMessageType.ADD_USER_TO_FRIENDS;
        ClientToServerMessage message = new ClientToServerMessage(type, userToAdd);
        sendMessage(message);
        return true;
    }
    /**
     * @param friendToAdd whose invitation we accepted
     * Sends message to server that we are now friends with friendToAdd
     * Add friend's nickname to friends ArrayList
     * */
    public static void confirmFriendship(String friendToAdd) {
        if(checkFriendship( friendToAdd)) {
            return;
        }
        friends.add(friendToAdd);
        ClientToServerMessageType type = ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP;
        ClientToServerMessage message = new ClientToServerMessage(type, friendToAdd);
        sendMessage(message);
    }

    /**
     * @param friendUsername nickname that we want to check
     * @return true if username in on our fiends ArrayList
     * */
    private static boolean checkFriendship(String friendUsername){
        return friends.contains(friendUsername);
    }

    /**
     * @param userToSend to who we want to send the message
     * @param text what text we want to send
     * Sends to out friend a text message
     * */
    public static void sendTextMessageToUser(String userToSend, String text){
        if( !checkFriendship(userToSend) ){
            return;
        }

        if( text.contains("#") || userToSend.contains("#") ){
            return;
        }

        String textMessage = userToSend + "#" + text;
        ClientToServerMessageType type = ClientToServerMessageType.TEXT_TO_USER;
        ClientToServerMessage message = new ClientToServerMessage(type, textMessage);
        sendMessage(message);

    }

    /**
     * @param groupName name of group we want to check
     * @return true if we belong to that group
     * Checks if groupName in on our groups ArrayList
     * */
    private static boolean checkMembership(String groupName){
        return groups.contains(groupName);
    }

    /**
     * @param groupName name of group we want to create
     * @throws Exception if: we are in such group, name contains '#', more than one word, shorter than 3 characters
     * Sends message to server a request to create a group
     * */
    public static void createGroup(String groupName) throws Exception
    {
        if( checkMembership(groupName))
        {
            throw new Exception("You are in such group already");
        }
        if( groupName.contains("#") )
        {
            throw new Exception("Cannot use '#'");
        }
        if( groupName.contains(" ") )
        {
            throw new Exception("Group name must be one word");
        }
        if( groupName.length() < 3 )
        {
            throw new Exception("Group name must have at least 3 characters");
        }

        ClientToServerMessageType type = ClientToServerMessageType.CREATE_GROUP;
        ClientToServerMessage message = new ClientToServerMessage(type, groupName);
        sendMessage( message );

    }

    /**
     * @param groupName to which group we want to send the message
     * @param text what text we want to send
     * checks if we are the member of the given group
     * if yes, we send it
     * if no, we do nothing
     * */
    public static void sendTextMessageToGroup(String groupName, String text)
    {
        if( !checkMembership(groupName) ){
            //System.out.println("You are not a part of this group");
            return;
        }
        ClientToServerMessageType type = ClientToServerMessageType.TEXT_TO_GROUP;
        String messageText = groupName + "#"+ username + "#" + text;
        ClientToServerMessage message = new ClientToServerMessage(type, messageText);
        sendMessage( message );
    }

    /**
     * @param group to which group we want to add a user
     * @param user nickanme of user we want to add
     * @throws Exception if we are not a member of given group or user is not our friend or we want to add ourselfs
     * */
    public static void addUserToGroup(String group, String user) throws Exception
    {

        if( !checkMembership(group))
        {
            throw new Exception("You are not a member of this group");
        }
        if( !checkFriendship(user))
        {
            throw new Exception("you cannot add this user to group because he is not your friend");
        }
        if( user.equals(username))
        {
            throw new Exception("You cannot add yourself to group");
        }
        ClientToServerMessageType type = ClientToServerMessageType.ADD_USER_TO_GROUP;
        String text = group + "#" + user;
        ClientToServerMessage message = new ClientToServerMessage(type, text);
        sendMessage(message);
    }


    /**
     * Sends LOGOUT_MESSAGE to server
     * */
    public static void logout(){
        ClientToServerMessage message = new ClientToServerMessage( ClientToServerMessageType.LOGOUT);
        System.out.println("Wylogowywanie...");
        username = null;

        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeClient(){
        try {
            outObject.close();
            inObject.close();
            echoSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




}