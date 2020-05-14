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
    private JButton startChat = new JButton("START CONVERSATION");
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
            tabs.addTab(FriendName, new ChatWindow(Username));
            return false;
        }
        else
            return true;

    }

    public MainWindow(String user) throws IOException
    {
        panel =  new MainTab(this,user);

        panel1 = new AddFriendPanel();
        Username = user;
        startChat.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                tabs.addTab("CZAT",new ChatWindow(Username));
            }
        });

        setTitle("KOMUNIKATOR XD");


        InitiateTabs();
        add(tabs);
    }


}
