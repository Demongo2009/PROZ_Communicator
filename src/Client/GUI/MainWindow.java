package Client.GUI;

import Client.Client;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import static Client.Client.addUserToFriends;
import static Client.Client.logout;

public class MainWindow extends JFrame
{
    private String Username="IGOR";
    private JTabbedPane tabs = new JTabbedPane();
    private JPanel panel;

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

    void SendFriendRequest(String AddedFriend)
    {
        addUserToFriends(AddedFriend);
        //WYSYLANIE ZAPROSZENIA DO ZNAJOMYCH (DO SERWERA)
    }


    public MainWindow(String user) throws IOException
    {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {

                super.windowClosing(e);
                System.out.println("WLASNIE SIE ZESRALEM XDDDD");
                logout();
                //System.exit(0);
                dispose();
            }
        });
        MainWindow tmpRef =this;
        panel =  new MainTab(this,user);
        tabs.addTab("MAIN PANEL",panel);
        Username = user;
        add(tabs);




    }




}
