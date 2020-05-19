package Client.GUI.Chat;

import Client.GUI.Main.MainWindow;

import javax.swing.*;

public class ChatsTab extends JTabbedPane {
    private MainWindow referenceToMainWindow;
    private JTabbedPane UsersGroups = new JTabbedPane();
    private JTabbedPane Users = new JTabbedPane();
    private JTabbedPane Groups = new JTabbedPane();

    public void chatWriteMessage(String sender, String msg) {
        ((ChatWindow) Users.getComponentAt(Users.indexOfTab(sender))).receiveMessage(msg);
    }

    public void groupWriteMessage(String groupName, String sender, String msg)
    {
        ((GroupChatWindow)Groups.getComponentAt(Groups.indexOfTab(groupName))).receiveMessage(msg,sender);
    }

    public boolean checkChat(String title)
    {
        if(Users.indexOfTab(title)==-1)
            return true;
        return false;
    }

    public boolean checkGroup(String title)
    {
        if(Groups.indexOfTab(title)==-1)
            return true;
        return false;
    }

    public boolean addChat(String friendName, String username)
    {
        if(checkChat(friendName))
        {
            Users.addTab(friendName,new ChatWindow(username,friendName,referenceToMainWindow));
            return true;
        }
        return false;
    }

    public boolean addGroupChat(String groupName, String username)
    {
        if(checkGroup(groupName))
        {
            Groups.addTab(groupName,new GroupChatWindow(username,groupName,referenceToMainWindow));
            return true;
        }
        return false;
    }

    public void closeChat(String name)
    {
        Users.removeTabAt(Users.indexOfTab(name));
    }

    public void closeGroupChat(String name)
    {
        Groups.removeTabAt(Groups.indexOfTab(name));
    }

    public void goToChatTab(String chatName)
    {
        setSelectedIndex(indexOfTab("Users"));
        Users.setSelectedIndex(Users.indexOfTab(chatName));
    }
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
