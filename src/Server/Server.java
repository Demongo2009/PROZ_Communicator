package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


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
    /**
     * inits server
     * */
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

    /**
     * accepts all clients and gives them a seperate thread
     * */
    private static void handleServer(){
        // Main program. It should handle all connections.
        try{
            while(true){
                Socket clientSocket= serverSocket.accept();
                ServerThread thread = new ServerThread(clientSocket, connectedUsers, groups);
                thread.start();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
