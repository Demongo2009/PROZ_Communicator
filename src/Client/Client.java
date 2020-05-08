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
    //static ArrayList<String> friends;

    public static void main(String[] args) {
       initClient();

       // TEST: wait until you get logged in
        String login = "Konrad2";
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
/*=====================================*/
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        System.out.println("Enter logout ");

        while( true ){
            if( myObj.nextLine().equals("logout")){
                logout();
                break;
            }
        }
/*=====================================*/
       //logout();
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

            ClientPrinterThread printerThread = new ClientPrinterThread(in);


            outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
            inObject = new ObjectInputStream( echoSocket.getInputStream() );




        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    * Sends to server LOGIN_REQUEST or REGISTER_REQUEST with login and password. Depends on given type argument
    * Throws Exception if type is none of above
    * */
    static void sendLoginOrRegisterRequest(String login, String password, ClientToServerMessageType type) throws Exception{
        if( type != ClientToServerMessageType.REQUEST_LOGIN && type != ClientToServerMessageType.REQUEST_REGISTER){
            throw new Exception("Only REQUEST_LOGIN or REQUEST_REGISTER");
        }

        String textToSend = login + "#" + password;
        ClientToServerMessage message = new ClientToServerMessage(type, textToSend, CommunicatorType.MULTI_COM );

        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * Throws exception if received message is not CONFIRM nor REJECT
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
            return true;
        }else{
            throw new Exception();
        }

    }

    /*
    * Sends LOGOUT_MESSAGE
    * */
    static void logout(){
         ClientToServerMessage message = new ClientToServerMessage( ClientToServerMessageType.LOGOUT);
         System.out.println("Wylogowywanie...");
         username = null;
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//TODO:
    static void sendTextMessageToUser(String user, String text){

    }

    static void sendTextMessageToGroup(String groupName,String text){

    }



}

