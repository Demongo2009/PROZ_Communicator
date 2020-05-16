package Server;

import java.util.ArrayList;

public class Group {
    private String name;

    private ArrayList<String> users;


    public String getGroupName(){ return name;}
    public boolean addUser(String newUser){
        if(users.size() < 4 ){
            users.add(newUser);
            return true;
        }
        return false;
    }
    public ArrayList<String> getUsers(){ return users; }

    public int getSize(){ return users.size(); }

    public String getUser(int index){ return users.get(index); }

    public Group(String groupName){
        users = new ArrayList<String>();
        name = groupName;
    }
}
