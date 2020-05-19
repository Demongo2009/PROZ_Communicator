package Client.GUI.Main;

import Client.GUI.Chat.ChatsTab;
import Client.GUI.Notifications.GuiNotificationListener;
import Client.GUI.Notifications.NotificationPanel;

import javax.swing.*;
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
    private NotificationPanel Notifications;
    private ChatsTab Chats;
    private GuiNotificationListener listener;

    boolean OpenChatWindow(String friendName)
    {
        return Chats.addChat(friendName,username);
    }
    boolean OpenGroupChatWindow(String GroupName)
    {
        return  Chats.addGroupChat(GroupName,username);
    }

    public void CloseChatWindow(String closedTabUserName)
    {
        Chats.closeChat(closedTabUserName);
    }
    public void CloseGroupChatWindow(String closedTabName)
    {
        Chats.closeGroupChat(closedTabName);
    }


    public void getMessageFromGroup(String serverMessage)
    {
        String []userAndText = serverMessage.split("#");
        String groupName = userAndText[0];
        String sendingUser = userAndText[1];
        String msgContent = userAndText[2];
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(Chats.checkGroup(groupName))
            Chats.addGroupChat(groupName,username);
        Chats.groupWriteMessage(groupName,sendingUser,msgContent);

    }

    public void getMessageFromUser(String serverMessage)
    {
        String []userAndText = serverMessage.split("#");
        String sender = userAndText[0];
        String messageText = userAndText[1];
        /*Jezeli ta karta czatu nie jest otwarta to otworz*/
        if(Chats.checkChat(sender))
            Chats.addChat(sender,username);
        Chats.chatWriteMessage(sender,messageText);

    }

    void goToChatTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        Chats.goToChatTab(name);

    }

    void goToGroupTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        Chats.goToGroupTab(name);
    }

    public void receiveFriendRequest(String serverMessage)
    {
        Notifications.receiveFriendRequest(serverMessage);
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
        Chats = new ChatsTab(tmpRef);
        Notifications = new NotificationPanel(tmpRef);
        panel =  new MainTab(this,user);
        tabs.addTab("MAIN PANEL",panel);
        tabs.addTab("Notifications",Notifications);
        tabs.add("Chats",Chats);
        username = user;
        add(tabs);
        listener = new GuiNotificationListener(this);
        listener.start();


    }

}
