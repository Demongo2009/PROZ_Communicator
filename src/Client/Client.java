package Client;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    static String hostName = "localhost";
    static int serverPort = 4444;

    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static BufferedReader stdIn;

    /* to send obejects */
    static ObjectOutputStream outObject;
    /* to receive objects */
    static ObjectInputStream inObject;

    static String username;


    public static void main(String[] args) {
       initClient();

       // TEST: wait until you get logged in
       while( true ) {
           sendLoginOrRegisterRequest("Konrad", "2", ClientToServerMessageType.REQUEST_LOGIN);
           try {
               if (receiveLoginAnswer()){
                   System.out.println("Client: udało się");
                   break;
               }else{
                   System.out.println("Clien: nie udało się");
                   break;
               }
           } catch (Exception e) {
               e.printStackTrace();
           }
       }
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


        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

     static void sendLoginOrRegisterRequest(String login, String password, ClientToServerMessageType type){
        String textToSend = login + "#" + password;
        ClientToServerMessage message = new ClientToServerMessage(type, textToSend);

        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
     }

    /* Throws exception if message given is not CONFIRM nor REJECT*/
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

}

