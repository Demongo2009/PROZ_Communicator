package Client.GUI.Main;

import Client.GUI.Chat.ChatsTab;
import Client.GUI.Notifications.GuiNotificationListener;
import Client.GUI.Notifications.NotificationPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import static Client.Client.*;

/*TODO: ZROBIC WSZYSTKIE PRZYPADKI POWIADOMIEN I ICH OBSLUZENIA
* Zrobic zakladke dla kazdego rodzaju powiadomien
* zrobic panel NotificationPanel i on bedzie wyswietlal w formie listy wszystkie powiadomienia
*
*

* */


public class MainWindow extends JFrame
{
    private String username ="IGOR";
    private JTabbedPane tabs = new JTabbedPane();
    private MainTab panel;
    private NotificationPanel notifications;
    private ChatsTab chats;
    private GuiNotificationListener listener;

    boolean OpenChatWindow(String friendName)
    {
        return chats.addChat(friendName,username);
    }
    boolean OpenGroupChatWindow(String GroupName)
    {
        return  chats.addGroupChat(GroupName,username);
    }

    public void CloseChatWindow(String closedTabUserName)
    {
        chats.closeChat(closedTabUserName);
    }
    public void CloseGroupChatWindow(String closedTabName)
    {
        chats.closeGroupChat(closedTabName);
    }


    public void getMessageFromGroup(String serverMessage)
    {
        String []userAndText = serverMessage.split("#");
        String groupName = userAndText[0];
        String sendingUser = userAndText[1];
        String msgContent = userAndText[2];
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(chats.checkGroup(groupName))
            chats.addGroupChat(groupName,username);
        chats.groupWriteMessage(groupName,sendingUser,msgContent);

    }

    public void getMessageFromUser(String serverMessage)
    {
        String []userAndText = serverMessage.split("#");
        String sender = userAndText[0];
        String messageText = userAndText[1];
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(chats.checkChat(sender))
            chats.addChat(sender,username);
        chats.chatWriteMessage(sender,messageText);

    }

    public void serverAlert(String chatName,String servertAlert)
    {
        if(chats.checkChat(chatName))
            chats.addChat(chatName,username);
        chats.chatWriteMessage(chatName,servertAlert);
    }

    public void setAlert(Color fg,String text)
    {
        panel.setState(fg,text);
    }

    void goToChatTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        chats.goToChatTab(name);

    }

    void goToGroupTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        chats.goToGroupTab(name);
    }

    public void receiveFriendRequest(String serverMessage)
    {
        notifications.receiveFriendRequest(serverMessage);
    }
    public void receiveGroupInvitation(String serverMessage)
    {
        notifications.receiveGroupInvitation(serverMessage);
    }

    public void refresh()
    {
        panel.refreshFriends();
        panel.refreshGroups();
    }


    public MainWindow(String user) throws IOException
    {
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                super.windowClosing(e);
                logout();
                dispose();
            }
        });
        MainWindow tmpRef =this;
        chats = new ChatsTab(tmpRef);
        notifications = new NotificationPanel(tmpRef);
        panel =  new MainTab(this,user);
        tabs.addTab("MAIN PANEL",panel);
        tabs.addTab("Notifications", notifications);
        tabs.add("Chats", chats);
        username = user;
        add(tabs);
        listener = new GuiNotificationListener(this);
        listener.start();


    }

}
