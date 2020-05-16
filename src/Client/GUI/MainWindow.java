package Client.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import static Client.GUI.tools.SwingConsole.*;

public class MainWindow extends JFrame
{
    private String Username="IGOR";
    private JTabbedPane tabs = new JTabbedPane();
    private JPanel panel;
    private JPanel panel1;
    //private JLabel are = new JLabel("",SwingConstants.CENTER);
    //private int openedTabs=2;
    private void InitiateTabs()
    {
        tabs.addTab("MAIN PANEL",panel);
        tabs.addTab("ADD FRIEND",panel1);

    }
    boolean OpenChatWindow(String FriendName)
    {
        if(tabs.indexOfTab(FriendName)==-1)
        {
            tabs.addTab(FriendName, new ChatWindow(Username,FriendName,this));
            return false;
        }
        else
            return true;

    }
    void CloseChatWindow(String closedTabUserName)
    {
        tabs.removeTabAt(tabs.indexOfTab(closedTabUserName));
    }
    public MainWindow(String user) throws IOException
    {
        MainWindow tmpRef =this;
        panel =  new MainTab(this,user);
        panel1 = new AddFriendPanel();
        Username = user;
        InitiateTabs();
        add(tabs);
    }


}
