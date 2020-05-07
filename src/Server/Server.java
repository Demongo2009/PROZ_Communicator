package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Server {
    static int serverPort = 4444;
    static ServerSocket serverSocket;
    static Map connectedUsers;

    public static void main(String[] args) {
        initServer();
        handleServer();

    }
    private static void initServer(){
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectedUsers = new HashMap();
    }

    private static void handleServer(){
        // Main program. It should handle all connections.

        try{
            ServerPrinterThread serverPrinterThread = new ServerPrinterThread();
            serverPrinterThread.start();
            while(true){
                Socket clientSocket= serverSocket.accept();
                ServerThread thread = new ServerThread(serverSocket, clientSocket, serverPrinterThread, connectedUsers);
                thread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}

