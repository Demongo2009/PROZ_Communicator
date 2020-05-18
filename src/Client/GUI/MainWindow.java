package Client.GUI;

import Client.Client;
import Messages.serverToClient.ServerToClientMessage;
import Server.Group;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import static Client.Client.addUserToFriends;
import static Client.Client.getNotification;
import static Client.GUI.tools.SwingConsole.*;

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
    boolean OpenGroupChatWindow(String GroupName)
    {
        if(tabs.indexOfTab(GroupName)==-1)
        {
            tabs.addTab(GroupName, new GroupChatWindow(Username,GroupName,this));
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


    /*Otrzymalismy informacje, ze cos przyszlo - sprawdzmy co to*/
    public void ReceiveNotification()
    {
        ServerToClientMessage received = getNotification();


        String []tmp = received.getText().split("#");
        String messageSender = tmp[0];
        String messageContent = tmp[1];
        switch (received.getType())
        {
            case CONFIRM_LOGIN:
            {

            }
            case REJECT_LOGIN:
            {

            }
            case LOGOUT:
            {

            }
            case GROUP_NAME_OCCUPIED:
            {

            }
            case USER_IS_NOT_CONNECTED:
            {

            }
            case TEXT_MESSAGE_FROM_USER:
            {
                getMessageFromUser(messageSender,messageContent);
            }
            case TEXT_MESSAGE_FROM_GROUP:
            {
                /*tmp0 = group name, tmp1 = sender, tmp2 = content*/
                getMessageFromGroup(tmp[0],tmp[1],tmp[2]);
            }
            case USER_ADDED_YOU_TO_GROUP:
            {

            }
            case USER_WANTS_TO_BE_YOUR_FRIEND:
            {

            }
            case USER_ACCEPTED_YOUR_FRIEND_REQUEST:
            {

            }
        }

    }



    private void getMessageFromGroup(String groupName,String Sender,String Content)
    {
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(tabs.indexOfTab(groupName)==-1)
        {
            tabs.addTab(groupName,new GroupChatWindow(Username,groupName,this));
        }
        else
        {
            //WYSLIJ POWIADOMIENIE
            System.out.println("XDDDD");
        }

        //Wypisz te wiadomosc
        ((GroupChatWindow)tabs.getTabComponentAt(tabs.indexOfTab(groupName))).receiveMessage(Content,Sender);

    }

    private void getMessageFromUser(String sender, String messageText)
    {
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(tabs.indexOfTab(sender)==-1)
        {
            tabs.addTab(sender, new ChatWindow(Username, sender, this));
        }
        //jezeli to okno czatu nie ma aktualnie focusu to niech pojawi sie powiadomienie
        if(!tabs.getTabComponentAt(tabs.indexOfTab(sender)).hasFocus())
        {

            //WYSLIJ POWIADOMIENIE
            System.out.println("XDDDD");
        }

        //wypisywanie wiadomosci na czacie
        ((ChatWindow)tabs.getTabComponentAt(tabs.indexOfTab(sender))).receiveMessage(messageText);

    }

    public MainWindow(String user) throws IOException
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {

                super.windowClosing(e);
                //logout();
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
