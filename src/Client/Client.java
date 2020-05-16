package Client;

import Client.GUI.*;
import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import Server.CommunicatorType;
import static Client.GUI.tools.SwingConsole.*;
import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
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
    static ArrayList<String> friends;
    static NotificationsHandler notificationsHandler;
    static ClientPrinterThread listener;

    public static void main(String[] args) {
       initClient();
        run(new StartingScreen(),"KOMUNIKATOR",300,100);
/*=====================================*/
        /*ZEBY ZADZIALALO TRZEBA ZMIENIC TRYB W PLIKU STARTINGSCREEN*/
/*=====================================*/
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

        String textToSend = login + "#" + password;
        ClientToServerMessage message = new ClientToServerMessage(type, textToSend, CommunicatorType.MULTI_COM );
        sendMessage( message );
    }

    /*
    * Throws exception if received message is not CONFIRM nor REJECT
    * starts listener thread
    * */
    public static boolean receiveLoginAnswer() throws Exception{
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


//TODO:
    static  void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void addUserToFriends(String userToAdd){
        for(String s: friends){
            if(userToAdd.equals(s)){
                System.out.println("User is already your friend");
                return;
            }
        }

        ClientToServerMessageType type = ClientToServerMessageType.ADD_USER_TO_FRIENDS;
        ClientToServerMessage message = new ClientToServerMessage(type, userToAdd);
        sendMessage(message);
    }

    static void sendTextMessageToUser(String userToSend, String text){
        String textMessage = userToSend + "#" + text;
        ClientToServerMessageType type = ClientToServerMessageType.TEXT_TO_USER;
        ClientToServerMessage message = new ClientToServerMessage(type, textMessage);
        sendMessage(message);

    }
    static void sendTextMessageToGroup(String groupName,String text){

    }
    //TODO: add exit function similar to logout
    static void exit(){

    }



}

