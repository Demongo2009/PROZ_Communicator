package Server;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler
{
    //private String url = "jdbc:sqlite:/home/demongo/EITI/PROZ/PROZ_Communicator/src/MultiCom.db";
    private String url = "jdbc:sqlite:S:/Programowanie/JAVA/PROZ_Communicatorl/srcMultiCom.db";
/*
users(
login VARCHAR(20) NOT NULL,
password VARCHAR(20) NOT NULL
);

friends(
user1 VARCHAR(20) NOT NULL,
user2 VARCHAR(20) NOT NULL
);


groups(
group_name VARCHAR(20) NOT NULL,
user1 VARCHAR(20) NOT NULL,
user2 VARCHAR(20),
user3 VARCHAR(20),
user4 VARCHAR(20)
)

 */
//TODO: make in resistant to sql injection
    ResultSet getLoginResultSet(Connection conn, Statement statement, String login){
        ResultSet rs = null;
        try {
            String query = "SELECT * FROM users WHERE login = \"" + login + "\"";
            rs = statement.executeQuery(query);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rs;
    }



    /*
    * Returns true if given login exists in given ResultSet
    * */
    boolean checkIfLoginExists(ResultSet rs, String login){
        try {
            while (rs.next()) {
                if( rs.getString("login").equals(login)){
                    return true;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
    /*
    * Returns true if password matches login
    * */
    boolean checkLogin(String login, String password){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        boolean answer = false;
        try {
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            rs = getLoginResultSet(conn, statement, login);

            if( rs.next() ){/*ok since there is one or zero records*/
                answer = rs.getString("password").equals(password);
            }


            statement.close();
            conn.close();
        }catch( Exception e){
            e.printStackTrace();
        }
        return answer;
    }
    /*
    * Return true if registration is successful
    * */
    boolean registerUser(String login, String password){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean successful = true;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            rs = getLoginResultSet(conn, statement, login);
            if( checkIfLoginExists(rs, login)){ /* if such login already exists*/
                successful = false;
            }

            if( successful ) {
                String query = "INSERT INTO users VALUES (\"" + login + "\", \"" + password + "\")";
                statement.executeUpdate(query);
            }


            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return successful;
    }

    /*
    * Returns true if given users are friends
    * */
    boolean checkFriendship(String user1, String user2){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        boolean areTheyFriends = false;

        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT * FROM friends WHERE (user1 = \"" + user1 + "\" AND user2 = \"" + user2 +"\") OR (user1 = \"" + user2  +"\" AND user2 = \"" + user1 + "\")";
            rs = statement.executeQuery(query);
            if( rs.next() ){
                areTheyFriends = true;
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return areTheyFriends;
    }

    /*
    * Inserts users' logins to 'friends' table
    * */
    void insertFriendship(String user1, String user2){
        Statement statement = null;
        Connection conn = null;
        try{

            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();


            String query = "INSERT INTO friends VALUES (\"" + user1 + "\", \"" + user2 + "\")";

            statement.executeUpdate(query);

            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /*
    * Returns user's friends' nicknames separated by '#'
    * */
    String getUserFriends(String login){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        String friends = "";
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT * FROM friends WHERE (user1 = \"" + login + "\") OR (user2 = \"" + login +"\")";
            rs = statement.executeQuery(query);
            while( rs.next() ){
                if( rs.getString("user1").equals(login) ){
                    friends += rs.getString("user2") + "#";
                }else{
                    friends += rs.getString("user1") + "#";
                }
            }
            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return friends;
    }


    ArrayList<Group> getGroups(){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;


        ArrayList<Group> groups = new ArrayList<Group>();
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT * FROM groups ";
            rs = statement.executeQuery(query);
            while( rs.next()){
                Group newGroup = new Group( rs.getString("group_name"));
                if( rs.getString("user1") != null ) {
                    newGroup.addUser(rs.getString("user1"));
                }
                if( rs.getString("user2") != null ){
                    newGroup.addUser( rs.getString("user2"));
                }
                if( rs.getString("user3") != null ){
                    newGroup.addUser( rs.getString("user3"));
                }
                if( rs.getString("user4") != null ){
                    newGroup.addUser( rs.getString("user4"));
                }

                groups.add(newGroup);
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return groups;
    }

    /*dont know if necessary since server holds an array list for groups*/
    String getUserGroups(String user){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        String groups = "";

        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT group_name FROM groups WHERE user1 = \"" + user +  "\" OR user2 = \"" + user + "\" OR user3 = \"" + user  + "\" OR user4 = \"" + user + "\"";
            rs = statement.executeQuery(query);
            while( rs.next()){
                groups += rs.getString("group_name") + "#";
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return groups;
    }

    boolean checkIfGroupExists(String groupName){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean exists = false;


        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT * FROM groups WHERE group_name =\""+groupName+"\"";
            rs = statement.executeQuery(query);
            if( rs.next() ){
                exists = true;
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return exists;
    }

    boolean checkIfUserExists(String login){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean exists = false;

        try {
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "SELECT * FROM users WHERE login = \"" + login +"\"";
            rs = statement.executeQuery(query);
            if( rs.next()){
                exists = true;
            }

            statement.close();
            conn.close();
        }catch( Exception e){
            e.printStackTrace();
        }
        return exists;
    }

    //TODO test it
    void createGroup(Group group){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            String query = "INSERT INTO groups(group_name, user1) VALUES (\""+ group.getGroupName() +"\",\"" + group.getUser(0) + "\")";
//            statement.executeQuery(query);
            statement.executeUpdate(query);
            //rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //TODO:

    boolean addUserToGroup(String group, String user){
        Statement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();

            String query = "SELECT * FROM groups WHERE group_name = \""+ group + "\"";
            rs = statement.executeQuery(query);
            if( rs.next() ){
            //should always happen since it was already checked

                String whichColumn = "";
                if( rs.getString("user2") == null){
                    whichColumn = "user2";
                }else if( rs.getString("user3") == null){
                    whichColumn = "user3";
                }else if( rs.getString("user4") == null){
                    whichColumn = "user4";
                }else{
                    System.out.println("GROUP IS FULL");//should never occur since it was already checked
                }
                query = "UPDATE groups SET " + whichColumn + " = \"" + user + "\"";
                statement.executeUpdate(query);
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return true;
    }
}
