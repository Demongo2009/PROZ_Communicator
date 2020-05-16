package Client;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import Server.CommunicatorType;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Client {

    static String hostName = "localhost";
    static int serverPort = 4444;

    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static BufferedReader stdIn;

    /* to send and receive objects */
    static ObjectOutputStream outObject;
    static ObjectInputStream inObject;

    static String username = null;
    static NotificationsHandler notificationsHandler;
    static ClientPrinterThread listener;

    static ArrayList<String> friends;
    static ArrayList<String> groups;

    // static GUI_notificiation_listener (notificaionHandler)
    //TODO: checking if we dont want to add ourselfs to friends, if we already arent friends etc
    /*TODO: if I get USER_ACCEPTED_YOUR_FRIEND_REQUEST message, then add friend to my local friends
        if can be done in the listener thread since its automatic, don't know if should, probably no because listener thread doesn't get our friends array*/


    public static void main(String[] args) {
       initClient();



        String login = "Konrad";
        String password = "123";
       try {
           sendLoginOrRegisterRequest(login, password, ClientToServerMessageType.REQUEST_LOGIN);
           if (receiveLoginAnswer()){
               System.out.println("Client: udało się zalogować");
               username = login;
           }else{
               System.out.println("Client: NIE udało się zalogować");
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
        Scanner myObj = new Scanner(System.in);
        System.out.println("Enter 'a' to continue");

        while( true ){
            if( myObj.nextLine().equals("a")){
                //addUserToGroup("Essssa", "Konrad2");
                createGroup("GRUPA");
                //confirmFriendship("Konrad");
                //sendTextMessageToUser("Konrad", "XDDDDDDDD");
                break;
            }
        }
        for(String s :friends){
            System.out.println(s);
        }
        for(String s: groups){
            System.out.println(s);
        }
        logout();
    }

    private static void initClient(){
        try {
            echoSocket = new Socket(hostName, serverPort);
            // shutdown hook added for closing the connection if client exits
            Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));


            out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
            stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));




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
    /*
    * Sends to server LOGIN_REQUEST or REGISTER_REQUEST(decided by argument) with login and password.
    * Throws Exception if type is none of above OR login or password contain '#' OR they are shorter than 4 characters
    * */
    static void sendLoginOrRegisterRequest(String login, String password, ClientToServerMessageType type) throws Exception{
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

        String textToSend = login + "#" + password;
        ClientToServerMessage message = new ClientToServerMessage(type, textToSend, CommunicatorType.MULTI_COM );
        sendMessage( message );
    }

    /*
    * Throws exception if received message is not CONFIRM nor REJECT
    * starts listener thread
    * */
    static boolean receiveLoginAnswer() throws Exception{
        ServerToClientMessage message = null;
        try {
            message = (ServerToClientMessage)inObject.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        ServerToClientMessageType response = message.getType();
        if( response == ServerToClientMessageType.REJECT_LOGIN ){
            return false;
        }else if( response == ServerToClientMessageType.CONFIRM_LOGIN ){

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
        }else{
            throw new Exception(" NOT CONFIRM NOR REJECTION");
        }

    }

    /*
    * Sends LOGOUT_MESSAGE
    * stops listener thread
    * */
    static void logout(){
        ClientToServerMessage message = new ClientToServerMessage( ClientToServerMessageType.LOGOUT);
        System.out.println("Wylogowywanie...");
        username = null;
        listener.stopRunning();
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addUserToFriends(String userToAdd){
        if( checkFriendship(userToAdd) ){
            System.out.println("User is already your friend!!!");
            return;
        }

        ClientToServerMessageType type = ClientToServerMessageType.ADD_USER_TO_FRIENDS;
        ClientToServerMessage message = new ClientToServerMessage(type, userToAdd);
        sendMessage(message);
    }
    /*
    * Sends message to server that we are now friends with friendToAdd
    * Add friend's nickname to friends ArrayList
    * */
    static void confirmFriendship(String friendToAdd){
        if(checkFriendship( friendToAdd)){
            return;
        }
        friends.add(friendToAdd);
        ClientToServerMessageType type = ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP;
        ClientToServerMessage message = new ClientToServerMessage(type, friendToAdd);
        sendMessage(message);
    }

    /*
    * Checks if username is on our friends Arraylist
    * */
    private static boolean checkFriendship(String friendUsername){
        return friends.contains(friendUsername);
    }

    /*
    * Sends to out friend a text message
    * */
    static void sendTextMessageToUser(String userToSend, String text){
        if( !checkFriendship(userToSend) ){
            System.out.println("User is not your friend -> you cannot write to him");
            return;
        }

        if( text.contains("#") || userToSend.contains("#") ){
            System.out.println("Using '#' is forbidden! ");
            return;
        }

        String textMessage = userToSend + "#" + text;
        ClientToServerMessageType type = ClientToServerMessageType.TEXT_TO_USER;
        ClientToServerMessage message = new ClientToServerMessage(type, textMessage);
        sendMessage(message);

    }

    /*
    * Checks if groupName in on our groups ArrayList
    * */
    private static boolean checkMembership(String groupName){
        return groups.contains(groupName);
    }

    /*
    * Sends message to server a request to create a group
    * */
    static void createGroup(String groupName){
        if( checkMembership(groupName)){
            System.out.println("You are in such group already");
            return;
        }
        if( groupName.contains("#") ){
            System.out.println("Cannot use '#'");
            return;
        }
        if( groupName.contains(" ") ){
            System.out.println("Group name must be one word");
            return;
        }
        if( groupName.length() < 3 ){
            System.out.println("Group name must have at least 3 characters");
            return;
        }

        ClientToServerMessageType type = ClientToServerMessageType.CREATE_GROUP;
        ClientToServerMessage message = new ClientToServerMessage(type, groupName);
        sendMessage( message );


    }

    /*
    *
    * */
    static void sendTextMessageToGroup(String groupName,String text){
        if(checkMembership(groupName)){
            System.out.println("You are not a part of this group");
            return;
        }
        ClientToServerMessageType type = ClientToServerMessageType.TEXT_TO_GROUP;
        String messageText = groupName + "#"+ username + "#" + text;
        ClientToServerMessage message = new ClientToServerMessage(type, messageText);
        sendMessage( message );

    }

    /*
    * Adds our friend to group we are into
    * */
    static void addUserToGroup(String group, String user){
        if( !checkMembership(group)){
            System.out.println("You are not a member of this group");
        }
        if( !checkFriendship(user)){
            System.out.println("you cannot add this user to group because he is not your friend");
        }
        ClientToServerMessageType type = ClientToServerMessageType.ADD_USER_TO_GROUP;
        String text = group + "#" + user;
        ClientToServerMessage message = new ClientToServerMessage(type, text);
        sendMessage(message);
    }


    //TODO: add exit function similar to logout
    static void exit(){

    }



}

