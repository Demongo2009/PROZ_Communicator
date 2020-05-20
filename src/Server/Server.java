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
    private static int serverPort = 9999;
    private static ServerSocket serverSocket;
    private static DatabaseHandler databaseHandler;
    private static ArrayList<User> connectedUsers;
    private static ArrayList<Group> groups;

    public static void main(String[] args) {
        initServer();
        handleServer();

    }
    private static void initServer(){
        databaseHandler = new DatabaseHandler();
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectedUsers = new ArrayList<User>();
        groups = databaseHandler.getGroups();
    }

    private static void handleServer(){
        // Main program. It should handle all connections.
        try{
            while(true){
                Socket clientSocket= serverSocket.accept();
                ServerThread thread = new ServerThread(serverSocket, clientSocket, connectedUsers, groups);
                thread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

    }
}

