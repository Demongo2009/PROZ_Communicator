package Server;

import java.sql.SQLOutput;

public class Test {
    static DatabaseHandler databaseHandler = new DatabaseHandler();

    public static void main(String[] args) {

        databaseHandler.setUrl("jdbc:sqlite:src/test.db");

        String user1 = "test";
        String pass1 = "pass";

        String user2 = "test2";
        String pass2 = "pass2";

        String user3 = "test3";
        String pass3 = "pass3";

        String user4 = "test4";
        String pass4 = "pass4";

        String user5 = "test5";
        String pass5 = "pass5";


        String groupName = "Grupa";
        Group group = new Group(groupName);


        try {

            registerUser(user1, pass1);
            registerUser(user2, pass2);
            registerUser(user3, pass3);
            registerUser(user4, pass4);
            registerUser(user5, pass5);

            //register user tho exists already with different passwords
            registerUserWhoExists(user1, user2);
            registerUserWhoExists(user5, user1);


            //Adding friends
            addFriendWhoAreNOTFriends(user1, user2);
            addFriendWhoAreNOTFriends(user1, user3);
            addFriendWhoAreNOTFriends(user1, user4);
            addFriendWhoAreNOTFriends(user1, user5);

            //Adding people who are friends already
            addFriendWhoAREFriends(user1, user2);



            //creating group
            if( databaseHandler.checkIfGroupExists(groupName)){
                throw new Exception("Group exist and it shouldn't");
            }
            group.addUser(user1);
            databaseHandler.createGroup(group);
            if( !databaseHandler.checkIfGroupExists(groupName) ){
                throw new Exception("Group does not exist and it should");
            }


            //adding to group
            addUserToGroupSuccessfully(groupName, user2);
            addUserToGroupSuccessfully(groupName, user3);
            addUserToGroupSuccessfully(groupName, user4);
            addUserToFullGroup(groupName, user5);

            System.out.println("TESTS FULLY PASSED");

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            databaseHandler.clearDataBase();
        }

    }

    static void registerUser(String login, String pass) throws Exception{
        if(databaseHandler.checkLogin(login, pass)) {
            throw new Exception("checkLogin() - before "+ login);
        }

        if( !databaseHandler.registerUser(login, pass)){
            throw new Exception("registerUser() - "+ login);
        }

        if( !databaseHandler.checkLogin(login, pass)) {
            throw new Exception("checkLogin() - after "+ login);
        }
    }

    static void registerUserWhoExists(String user, String pass)throws  Exception{
        if( databaseHandler.registerUser(user, pass)){
            throw new Exception("registerUser() who exists: "+ user);
        }
    }

    static void addFriendWhoAreNOTFriends(String user1, String user2) throws Exception{
        if( databaseHandler.checkFriendship(user1, user2)){
            throw new Exception(" checkFriendship() not friends - before " + user1 + "and "+ user2);
        }

        databaseHandler.insertFriendship(user1, user2);

        if( !databaseHandler.checkFriendship(user1, user2)){
            throw new Exception(" checkFriendship() friends - after " + user1 + "and "+ user2);
        }
    }

    static void addFriendWhoAREFriends(String user1, String user2)throws  Exception{
        if( !databaseHandler.checkFriendship(user1, user2)){
            throw new Exception(" checkFriendship() friends - before " + user1 + "and "+ user2);
        }

        databaseHandler.insertFriendship(user1, user2);

        if( !databaseHandler.checkFriendship(user1, user2)){
            throw new Exception(" checkFriendship() friends - after " + user1 + "and "+ user2);
        }

    }

    static void addUserToGroupSuccessfully(String groupName, String user) throws Exception{
        if( !databaseHandler.addUserToGroup(groupName, user) ){
            throw  new Exception("Cannot add user to group:" + user);
        }
    }

    static void addUserToFullGroup(String groupName, String user) throws  Exception{
        if( databaseHandler.addUserToGroup(groupName, user) ){
            throw  new Exception("Adding user to full group:" + user);
        }
    }

}
