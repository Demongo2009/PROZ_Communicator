package Server;

import java.util.ArrayList;

/**
 * Class to handle groups on server
 * */
public class Group {
    private String name;

    private ArrayList<String> users;


    String getGroupName(){ return name;}
    boolean addUser(String newUser){
        if(users.size() < 4 ){
            users.add(newUser);
            return true;
        }
        return false;
    }
    int getSize(){ return users.size(); }

    String getUser(int index){ return users.get(index); }

    Group(String groupName){
        users = new ArrayList<String>();
        name = groupName;
    }
}
