package Server;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseHandler
{
    private String url = "jdbc:sqlite:src/MultiCom.db";
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
    );
     */

    /**
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
    /**
     * Returns true if password matches login
     * */
    boolean checkLogin(String login, String password){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        boolean answer = false;
        try {
            conn = DriverManager.getConnection(url);

            statement = conn.prepareStatement("SELECT * FROM users WHERE login = ?");
            statement.setString(1, login);
            rs = statement.executeQuery();

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
    /**
     * Return true if registration is successful
     * */
    boolean registerUser(String login, String password){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean successful = true;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM users WHERE login = ?");
            statement.setString(1, login);
            rs = statement.executeQuery();



            if( checkIfLoginExists(rs, login)){ /* if such login already exists*/
                successful = false;
            }

            statement.close();
            if( successful ) {
                statement = conn.prepareStatement("INSERT INTO users VALUES( ?, ?)");
                statement.setString(1,login);
                statement.setString(2, password);
                statement.executeUpdate();
            }


            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return successful;
    }

    /**
     * Returns true if given users are friends
     * */
    boolean checkFriendship(String user1, String user2){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        boolean areTheyFriends = false;

        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM friends WHERE (user1 = ? AND user2 = ? ) OR (user1 = ? AND user2 = ? )");
            statement.setString(1, user1);
            statement.setString(2, user2);
            statement.setString(3, user2);
            statement.setString(4, user1);
            rs = statement.executeQuery();
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

    /**
     * Inserts users' logins to 'friends' table
     * */
    void insertFriendship(String user1, String user2){
        PreparedStatement statement = null;
        Connection conn = null;
        try{

            conn = DriverManager.getConnection(url);

            statement = conn.prepareStatement("INSERT INTO friends VALUES (?, ?)");
            statement.setString(1, user1);
            statement.setString(2, user2);

            statement.executeUpdate();

            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /**
     * Returns user's friends' nicknames separated by '#'
     * */
    String getUserFriends(String login){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        String friends = "";
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM friends WHERE (user1 = ?) OR (user2 = ?)");
            statement.setString(1, login);
            statement.setString(2, login);

            rs = statement.executeQuery();
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
            String query = "SELECT * FROM groups "; //no need to change it for prepared statement
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
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        String groups = "";

        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT group_name FROM groups WHERE user1 = ? OR user2 = ? OR user3 = ? OR user4 = ?");
            statement.setString(1, user);
            statement.setString(2, user);
            statement.setString(3, user);
            statement.setString(4, user);

            rs = statement.executeQuery();
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
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean exists = false;


        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM groups WHERE group_name = ?");
            statement.setString(1, groupName);

            rs = statement.executeQuery();
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
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;

        boolean exists = false;

        try {
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM users WHERE login = ?");
            statement.setString(1, login);
            rs = statement.executeQuery();
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

    void createGroup(Group group){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("INSERT INTO groups(group_name, user1) VALUES (?, + ?)");
            statement.setString(1, group.getGroupName());
            statement.setString(2, group.getUser(0));
            statement.executeUpdate();


            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    boolean addUserToGroup(String group, String user){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        boolean successful = true;

        try{


            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("SELECT * FROM groups WHERE group_name = ?;");
            statement.setString(1, group);
            rs = statement.executeQuery();

            String query = "";

            if( rs.next() ) {
                //should always happen since it should be already checked

                if (rs.getString("user2") == null) {
                    query = "UPDATE groups SET user2 = ? WHERE group_name = ?";
                } else if (rs.getString("user3") == null) {
                    query = "UPDATE groups SET user3 = ? WHERE group_name = ?";
                } else if (rs.getString("user4") == null) {
                    query = "UPDATE groups SET user4 = ? WHERE group_name = ?";
                } else {
                    //System.out.println("GROUP IS FULL");//should never occur since it was already checked
                    successful = false;
                }
            }

            if (successful) {
                statement.close();
                statement = conn.prepareStatement(query);

                statement.setString(1, user);
                statement.setString(2, group);
                statement.executeUpdate();
            }

            rs.close();
            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        return successful;
    }



    //for unit tests
    void setUrl(String newUrl){
        url = newUrl;
    }

    //for unit tests
    void clearDataBase(){
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet rs = null;
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.prepareStatement("DELETE FROM users");
            statement.executeUpdate();
            statement.close();

            statement = conn.prepareStatement("DELETE FROM friends");
            statement.executeUpdate();

            statement.close();
            statement = conn.prepareStatement("DELETE FROM groups");
            statement.executeUpdate();

            statement.close();
            conn.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}