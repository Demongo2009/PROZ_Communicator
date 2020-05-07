package Server;

import java.sql.*;

public class DatabaseHandler {
    private String url = "jdbc:sqlite:/home/konrad/Desktop/scratchpad/sem4/PROZ/PROZ_Communicator/src/users.db";
//TODO: make in resistent to sql injection
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
                answer = rs.getString("pass").equals(password);
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
        try{
            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            rs = getLoginResultSet(conn, statement, login);
            if( checkIfLoginExists(rs, login)){ /* if such login already exists*/
                rs.close();
                statement.close();
                conn.close();
                return false;
            }


            String query = "INSERT INTO users VALUES (\"" + login + "\", \"" + password + "\")";

            conn = DriverManager.getConnection(url);
            statement = conn.createStatement();
            statement.executeUpdate(query);

            rs.close();
            statement.close();
            conn.close();;
            return true;
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }



}
