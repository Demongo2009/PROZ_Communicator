package Server;

import Server.Server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class Protocol {
//    int clientAddress;
//    int clientPort;
    User client;

    Protocol(int clientAddress, int clientPort){
        client = new User("",clientAddress,clientPort);
//        this.clientAddress = clientAddress;
//        this.clientPort = clientPort;
    }


    enum AvailableStates {
        INIT,
        WAITING_FOR_USERNAME,
        SEARCHING_GROUP,
        CREATING_GROUP,
        CONNECTED_WITH_GROUP,
        CHOOSING_GROUP_ACTION,
    }

    AvailableStates state = AvailableStates.INIT;
//    String clientName;


//    String partnerName;
//
//    int partnerAddress;
//    int partnerPort;

//    Socket partnerSocket;
//    PrintWriter partnerOut;


    //TODO: ADDRESS AND PORT FORWARDING TO SERVERTHREAD TO ESTABILISH CONNECTION BETWEEN CLIENTS

// sqlite database handling
//////////////////////////////////////////////////////////////////

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
            }while(rs.next()){
//                partnerAddress = rs.getInt("Address");
//                partnerPort = rs.getInt("Port");
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

//        String query =
//                "insert into Users (Username, Address, Port)" +
//                        " Select ?,?,? Where not exists(select * from Users where Username=?)";

//        String query =
//                "INSERT INTO Users (Username, Address, Port) VALUES(?, ?, ?) ON DUPLICATE KEY UPDATE    " +
//                        "Address = ?, Port = ?";

//        String query =
//                "update Users set Username=?,Address=?,Port=? where Username=? " +
//                        "IF @@ROWCOUNT=0 " +
//                        "   insert into Users(Username,Address,Port) values(?,?,?);";

        Connection conn = null;
        try{
            String url = "jdbc:sqlite:/home/demongo/EITI/PROZ/PROZ_Communicator/src/UserNameDataBase";
            conn = DriverManager.getConnection(url);

            System.out.println("Connection to sql db established...");

            stmt = conn.prepareStatement(query);
            stmt.setString(1, input.replace("\n","") );
//            stmt.setString(2, input.replace("\n","") );
            stmt.setInt(2, client.getHashedAddress() );
            stmt.setInt(3, client.getPort() );
//            stmt.setString(4, input.replace("\n","") );
//            stmt.setString(5, input.replace("\n","") );

//            stmt.setString(4, input.replace("\n","") );
//            stmt.setInt(6, clientAddress );
//            stmt.setInt(7, clientPort );

            stmt.executeUpdate();


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

    ///////////////////////////////////////////////////


    boolean checkIfGroupExists(String groupId){
        if(Server.groupMap.containsKey(Integer.parseInt(groupId))){

            return true;
        }else {
            return false;
        }

    }

    ArrayList<User> currentGroupUsers;

    // main function of protocol state machine

    public String processInput(String input){
        // string to be sent by server
        String processedInput = "";

        //first message from server
        if(input == null){
            processedInput = "...";
            return processedInput;
        }
        // if client types "Quit" than connections is closed
        if(input == "Quit"){
            processedInput = "Bye.";
            return processedInput;
        }
        if(state.equals(AvailableStates.INIT)){
            state = AvailableStates.WAITING_FOR_USERNAME;
            processedInput = "Witam to jest Chat\n" +
                    "Najpierw podaj swoje imie...";

        }

        // client is supposed to give its name, than its added to database
        else if(state.equals(AvailableStates.WAITING_FOR_USERNAME)){

            client.setName(input);
            processedInput = "Twoje imie to: " + input;

            if(addToDB(input)){
                processedInput += "\nDodano imie.";
                processedInput += "\nDolacz do istniejacej grupy: d" +
                        "\n lub zaloz nowa: n";
                state = AvailableStates.CHOOSING_GROUP_ACTION;
            }else{
                processedInput += "\nNie dodano imienia.";
            }


            // client types name of his partner and if such name exists in database than it is checked if client is
            // connected to server by checking all connected socket on socked array


        }else if(state.equals(AvailableStates.CHOOSING_GROUP_ACTION)){


            switch (input){
                case "d":
                    state = AvailableStates.SEARCHING_GROUP;
                    processedInput = "Type in group id...";
                    break;
                case "n":
                    state = AvailableStates.CREATING_GROUP;
                    processedInput = "Creating group..." +
                            "\n Please confirm with any key...";
                    break;
                default:
                    processedInput = "Wrong input!";
                    break;
            }


        } else if(state.equals(AvailableStates.CREATING_GROUP)){

            int newIndex = Server.groupMap.size();
            ArrayList group = new ArrayList<User>();
            group.add(client);
            Server.groupMap.put(newIndex,group);
            currentGroupUsers = Server.groupMap.get(newIndex);

            processedInput = "Group created with id: "+newIndex;

            state = AvailableStates.CONNECTED_WITH_GROUP;
        }

        else if(state.equals(AvailableStates.SEARCHING_GROUP)){

            // checking if name exists
            if(checkIfGroupExists(input)){
                processedInput = "Group id  " + input + "  found!";
//                partnerName = input;

                ArrayList group = Server.groupMap.get(Integer.parseInt(input));
                group.add(client);
                Server.groupMap.replace(Integer.parseInt(input),group);
                currentGroupUsers = Server.groupMap.get(Integer.parseInt(input));
                state = AvailableStates.CONNECTED_WITH_GROUP;


//// checking all connected sockets
//                for(Socket s: Server.clientSocketArray){
//                    if( (s.getPort() == partnerPort) && (s.getInetAddress().hashCode() == partnerAddress) ){
//                        partnerSocket = s;
//                    }
//                }
//                // if partner not connected
//                if(partnerSocket == null){
//                    processedInput += " " + partnerName + " not connected.";
//                    return processedInput;
//                }
//// getting partner out
//                try {
//                    partnerOut = new PrintWriter(partnerSocket.getOutputStream(), true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                state = AvailableStates.CONNECTED_WITH_GROUP;



            }else {
                processedInput = "Group id  " + input + "  doesnt exist! Type in new...";
            }


// messages outputted on partner out
        }else if(state.equals(AvailableStates.CONNECTED_WITH_GROUP)){
            for(User u: currentGroupUsers){
                u.addToBuffer(client.getName() + ": " +input);
                u.trySend();
            }
//            partnerOut.println(clientName + ": " + input);

        }
//        processedInput= "odpowiedz";

        return processedInput;
    }
}