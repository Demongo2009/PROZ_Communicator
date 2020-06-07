package Client.GUI.Chat;

import Client.GUI.Main.MainWindow;

import javax.swing.*;

public class ChatsTab extends JTabbedPane {
    private MainWindow referenceToMainWindow;
    private JTabbedPane UsersGroups = new JTabbedPane();
    private JTabbedPane Users = new JTabbedPane();
    private JTabbedPane Groups = new JTabbedPane();


    /**
     * Write received message
     * @param sender from whom message
     * @param msg message content
     * */
    public void chatWriteMessage(String sender, String msg) {
        ((ChatWindow) Users.getComponentAt(Users.indexOfTab(sender))).receiveMessage(msg);
    }


    /**
     * Write received group message
     * @param groupName name of group
     * @param sender from whom message
     * @param msg message content
     * */
    public void groupWriteMessage(String groupName, String sender, String msg)
    {
        ((GroupChatWindow)Groups.getComponentAt(Groups.indexOfTab(groupName))).receiveMessage(msg,sender);
    }

    /**
     * Checks whether tab is already opened
     * @param title title of tab
     * @return true if such tab is already opened
     * */
    public boolean checkChat(String title)
    {
        if(Users.indexOfTab(title)==-1)
            return true;
        return false;
    }

    /**
     * checks if group chat tab is already opened
     * @param title title of tab
     * @return true if such tab is already opened
     * */
    public boolean checkGroup(String title)
    {
        if(Groups.indexOfTab(title)==-1)
            return true;
        return false;
    }

    /**
     * opens new chat tab
     * @param friendName name of a friend
     * @param username name of the user
     * @return true if such tab is already opened
     * */
    public boolean addChat(String friendName, String username)
    {
        if(checkChat(friendName))
        {
            Users.addTab(friendName,new ChatWindow(username,friendName,referenceToMainWindow));
            return true;
        }
        return false;
    }

    /**
     * opens new chat tab
     * @param groupName name of a group
     * @param username name of the user
     * @return true if such tab is already opened
     * */
    public boolean addGroupChat(String groupName, String username)
    {
        if(checkGroup(groupName))
        {
            Groups.addTab(groupName,new GroupChatWindow(username,groupName,referenceToMainWindow));
            return true;
        }
        return false;
    }


    /**
     * closes chat tab of given name
     * @param name  name of a closed tab
     * */
    public void closeChat(String name)
    {
        Users.removeTabAt(Users.indexOfTab(name));
    }

    /**
     * closes chat tab of given name
     * @param name  name of a closed tab
     * */
    public void closeGroupChat(String name)
    {
        Groups.removeTabAt(Groups.indexOfTab(name));
    }

    /**
     * GUI proceeds to specified tab
     * @param chatName name of tab, wanted to be proceeded to
     * */
    public void goToChatTab(String chatName)
    {
        setSelectedIndex(indexOfTab("Users"));
        Users.setSelectedIndex(Users.indexOfTab(chatName));
    }
    /**
     * GUI proceeds to specified tab
     * @param groupName name of tab, wanted to be proceeded to
     * */
    public void goToGroupTab(String groupName)
    {
        setSelectedIndex(indexOfTab("Groups"));
        Groups.setSelectedIndex(Groups.indexOfTab(groupName));
    }

    public ChatsTab(MainWindow referenceToMainWindow)
    {
        this.referenceToMainWindow = referenceToMainWindow;
        addTab("Users",Users);
        addTab("Groups",Groups);
    }

}
