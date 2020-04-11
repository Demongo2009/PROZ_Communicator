import javafx.event.Event;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class Protocol {
    int clientAddress;
    int clientPort;

    Protocol(int clientAddress, int clientPort){
        this.clientAddress = clientAddress;
        this.clientPort = clientPort;
    }


    enum AvailableStates {
        WAITING_FOR_USERNAME,
        SEARCHING_PARTNER,
        CONNECTED_WITH_PARTNER,
    }

    AvailableStates state = AvailableStates.WAITING_FOR_USERNAME;
    String clientName;


    String partnerName;

    int partnerAddress;
    int partnerPort;

    Socket partnerSocket;
    PrintWriter partnerOut;

//    private void getConnectionWithDB(){
////        Connection conn = null;
//        try{
//            String url = "jdbc:sqlite:/home/demongo/EITI/PROZ/PROZ_Communicator/src/UserNameDataBase";
//            conn = DriverManager.getConnection(url);
//
//            System.out.println("Connection to sql db established...");
//        } catch (SQLException e) {
//            e.printStackTrace();
//        } finally {
//            try{
//                if(conn != null){
//                    conn.close();
//                }
//
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
////        return conn;
//    }

    //TODO: ADDRESS AND PORT FORWARDING TO SERVERTHREAD TO ESTABILISH CONNECTION BETWEEN CLIENTS

    private boolean checkIfUserNameExists(String input){
        Statement stmt = null;
        String query = "select Username, Address, Port from Users where Username='"+input.replace("\n","")+"'";

        Connection conn = null;
        try{
            String url = "jdbc:sqlite:/home/demongo/EITI/PROZ/PROZ_Communicator/src/UserNameDataBase";
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to sql db established...");

            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(!rs.next()){
                return false;
            }
            while (rs.next()){
                partnerAddress = rs.getString("Address").hashCode();
                partnerPort = rs.getInt("Port");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try{
                if(conn != null){
                    conn.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

        return true;
    }

    private boolean addToDB(String input){
        PreparedStatement stmt = null;
        String query = "INSERT INTO Users (Username, Address, Port) " +
                "VALUES (?,?,?);";

        Connection conn = null;
        try{
            String url = "jdbc:sqlite:/home/demongo/EITI/PROZ/PROZ_Communicator/src/UserNameDataBase";
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to sql db established...");

            stmt = conn.prepareStatement(query);
            stmt.setString(1, input.replace("\n","") );
            stmt.setInt(2, clientAddress );
            stmt.setInt(3, clientPort );

            stmt.executeUpdate();
//            if(!stmt.execute()){
//                System.out.println("tu");
//                return false;
//            }
//            if(!rs.next()){
//               return false;
//            }
//            while (rs.next()){
//                partnerAddress = rs.getString("Address");
//                partnerPort = rs.getInt("Port");
//            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try{
                if(conn != null){
                    conn.close();
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

            if (stmt != null){
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }

        return true;
    }

    public String processInput(String input){
        String processedInput = "";

        if(input == null){
            processedInput = "Witam to jest Chat\n" +
                    "Najpierw podaj swoje imie...";
            return processedInput;
        }
        if(input == "Quit"){
            processedInput = "Bye.";
            return processedInput;
        }
//TODO: sqlite jdbc MAVEN

        if(state.equals(AvailableStates.WAITING_FOR_USERNAME)){

            clientName = input;
            processedInput = "Twoje imie to: " + input;

            if(addToDB(input)){
                processedInput += "\nDodano imie.";
                processedInput += "\nPodaj imie partnera...";
            }else{
                processedInput += "\nNie dodano imienia.";
            }

            state = AvailableStates.SEARCHING_PARTNER;

        }else if(state.equals(AvailableStates.SEARCHING_PARTNER)){
//            getConnectionWithDB();

            if(checkIfUserNameExists(input)){
                processedInput = "User name \" " + input + " \" found!";
                partnerName = input;



                for(Socket s: Server.clientSocketArray){
                    if( (s.getPort() == partnerPort) && (s.getInetAddress().hashCode() == partnerAddress) ){
                        partnerSocket = s;
                    }
                }
                if(partnerSocket == null){
                    processedInput = partnerName + " not connected.";
                    return processedInput;
                }

                try {
                    partnerOut = new PrintWriter(partnerSocket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                state = AvailableStates.CONNECTED_WITH_PARTNER;



            }else {
                processedInput = "User name \" " + input + " \" doesnt exist!";
            }



        }else if(state.equals(AvailableStates.CONNECTED_WITH_PARTNER)){
            partnerOut.println(clientName + ": " + input);

        }
//        processedInput= "odpowiedz";

        return processedInput;
    }
}
