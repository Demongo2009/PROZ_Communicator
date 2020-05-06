package Server;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
//import Server.Protocol;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import Server.ServerPrinterThread;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread{
    static Semaphore mutex;
    ServerSocket serverSocket;
    Socket clientSocket;
    ServerPrinterThread serverPrinterThread;

    /* to send obejects */
    static ObjectOutputStream outObject;
    /* to receive objects */
    static ObjectInputStream inObject;

    ServerThread(ServerSocket serverSocket, Socket clientSocket, ServerPrinterThread serverPrinterThread){
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.serverPrinterThread = serverPrinterThread;
        mutex = new Semaphore(1);

        try {
            outObject = new ObjectOutputStream( clientSocket.getOutputStream() );
            inObject = new ObjectInputStream(( clientSocket.getInputStream()) );
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void run() {

        PrintWriter out = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));


        } catch (IOException e) {
            e.printStackTrace();
        }


        while ( true ){
             if (sendLoginAnswer( processLoginOrRegisterRequest() ) ){
                 System.out.println("Serwer: użytkownik zalogowany");
                 break;
             }else{
                 System.out.println("Serwe: nie udało sie");
                 break;
             }
        }
    }

/* throws exception if message is not REQUEST_LOGIN
*   return true if login or register is succesful
* */
    boolean processLoginOrRegisterRequest(){
        String url = "jdbc:sqlite:/home/konrad/Desktop/scratchpad/sem4/PROZ/PROZ_Communicator/src/users.db";
        Statement statement = null;
        Connection conn = null;
        try {
            ClientToServerMessage message = (ClientToServerMessage)inObject.readObject();
            if( message.getType() != ClientToServerMessageType.REQUEST_LOGIN
                && message.getType() != ClientToServerMessageType.REQUEST_REGISTER){
                throw new Exception();
            }

            String[] loginAndPass = message.getString().split("#");

            conn = DriverManager.getConnection(url);
            String query = "SELECT * FROM users WHERE login = \"" + loginAndPass[0] + "\"";
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(query);
            statement.close();

            if( message.getType() == ClientToServerMessageType.REQUEST_LOGIN){
                return  checkLogin(rs, loginAndPass[1]);
            }else{
                return checkRegister(rs, loginAndPass[0], loginAndPass[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;// something went wrong
    }
/* returns true if password matches to login*/
    boolean checkLogin(ResultSet rs, String password){
        try {
                /*is OK since there is always only one user with the same login*/
            if( rs.next()) {
                return password.equals(rs.getString("pass"));
            }
        }catch( Exception e){
            e.printStackTrace();
        }
        return false;
    }
/*returns true if login does not occur
* inserts new user*/
    boolean checkRegister(ResultSet rs, String login, String password){
        Statement statement = null;
        Connection conn = null;
        String url = "jdbc:sqlite:/home/konrad/Desktop/scratchpad/sem4/PROZ/PROZ_Communicator/src/users.db";
        try{
            while( rs.next()) {
                if( login.equals( rs.getString("login") ) ) {
                    return false;
                }
            }
            String query = "INSERT INTO users VALUES (\"" + login + "\", \"" + password + "\")";

            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            statement.executeUpdate(query);
            statement.close();
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
/* returns true if answer is positive*/
    boolean sendLoginAnswer(boolean answer){
        ServerToClientMessageType type = null;
        if( answer ){
            type = ServerToClientMessageType.CONFIRM_LOGIN;
        }else{
            type = ServerToClientMessageType.REJECT_LOGIN;
        }

        ServerToClientMessage message = new ServerToClientMessage(type, "");

        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }
}
