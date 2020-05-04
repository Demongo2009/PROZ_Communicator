package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    static ArrayList<Socket> clientSocketArray = new ArrayList<Socket>();
    static HashMap<Integer,ArrayList<User>> groupMap;

    public static void main(String[] args) {
        System.out.println("Hello");
        int portNumber = 4444;
        groupMap= new HashMap<Integer,ArrayList<User>>();


// Main program. It should handle all connections.
        try{
            ServerSocket serverSocket = new ServerSocket(portNumber);

            ServerPrinterThread serverPrinterThread = new ServerPrinterThread();
            serverPrinterThread.start();

            while(true){
                Socket clientSocket= serverSocket.accept();
                clientSocketArray.add(clientSocket);
                ServerThread thread = new ServerThread(serverSocket, clientSocket, serverPrinterThread);
                thread.start();
            }

        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}

