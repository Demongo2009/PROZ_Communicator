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
    private JPanel panel = new JPanel();
    private JPanel panel1;
    private JLabel are = new JLabel("",SwingConstants.CENTER);
    private JButton startChat = new JButton("START CONVERSATION");
    private void InitiateTabs()
    {
        tabs.addTab("MAINTAB",panel);
        tabs.addTab("ADD FRIEND",panel1);

    }

    public MainWindow(String user) throws IOException {
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
        panel.setLayout(new BorderLayout());
        panel.add(are,BorderLayout.CENTER);
        panel.add(startChat,BorderLayout.SOUTH);

        InitiateTabs();
        add(tabs);
    }


}