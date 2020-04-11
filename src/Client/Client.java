package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    static void send(String[] args){




        String hostName = "localhost";
        int portNumber = 4444;

        try {
            Socket echoSocket = new Socket(hostName, portNumber);
            // shutdown hook added for closing the connection if client exits
            Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));

            PrintWriter out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));

            // thread for printing client messages
            ClientPrinterThread clientPrinterThread = new ClientPrinterThread(in);
            clientPrinterThread.start();


            String userInput;
            // sending message to server
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
//                System.out.println("echo: " + in.readLine());
            }
        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }



    }

    public static void main(String[] args) {
        System.out.println("Hello");
        send(args);
    }
}
