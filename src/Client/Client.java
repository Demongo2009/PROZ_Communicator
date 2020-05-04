package Client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    static String hostName = "localhost";
    static int portNumber = 4444;

    static void sendFromTerminal(){


        String userInput="";
        // sending message to server
        try{
            while ((userInput = stdIn.readLine()) != null) {

                out.println(userInput);
    //                System.out.println("echo: " + in.readLine());
            }
        }catch (IOException e) {
            e.printStackTrace();
        }


    }

    static void sendFromApp(String text){

        String userInput="";
            // sending message to server

        if (text != null) {

            out.println(text);
//                System.out.println("echo: " + in.readLine());
        }


    }
    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static BufferedReader stdIn;


    public static void main(String[] args) {
        ApplicationClient app = new ApplicationClient();

        app.show();


        try {
            echoSocket = new Socket(hostName, portNumber);
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

            // thread for printing client messages
            ClientPrinterThread clientPrinterThread = new ClientPrinterThread(in,app);
            clientPrinterThread.start();


            sendFromTerminal();
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
