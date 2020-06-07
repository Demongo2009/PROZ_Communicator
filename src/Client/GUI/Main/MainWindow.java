package Client.GUI.Main;

import Client.GUI.Chat.ChatsTab;
import Client.GUI.Notifications.GuiNotificationListener;
import Client.GUI.Notifications.NotificationPanel;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;

import static Client.Client.*;



public class MainWindow extends JFrame
{
    private String username ="IGOR";
    private String pathToNewMessageAlert = "src/Assets/newMessage.wav";
    private String pathToRequestAlert = "src/Assets/request.wav";
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

    /**
     * go and tell the GroupchatWindow that a message has been received
     * @param serverMessage received message
     * */
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
    /**
     * go and tell the chatWindow that a message has been received
     * @param serverMessage received message
     */
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
    /**
     * Informs user that receiver is unavailable
     * @param chatName name of a chat
     * @param servertAlert write what server has to say (it also needs to be listened to)
     * */
    public void serverAlert(String chatName,String servertAlert)
    {
        if(chats.checkChat(chatName))
            chats.addChat(chatName,username);
        chats.chatWriteMessage(chatName,servertAlert);
    }

    /**
     * Sets text of a state in main tab
     * @param fg color of the text
     * @param text text of the text
     * */
    public void setAlert(Color fg,String text)
    {
        panel.setState(fg,text);
    }

    /**
     * proceed to tab of given name
     * @param name name of a tab
     * */
    void goToChatTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        chats.goToChatTab(name);

    }

    /**
     * proceed to tab of given name
     * @param name name of a tab
     * */
    void goToGroupTab(String name)
    {
        tabs.setSelectedIndex(tabs.indexOfTab("Chats"));
        chats.goToGroupTab(name);
    }

    /**
     * Someone wants to befriend with us :)))
     * alerts notification panel about new invitation
     * @param serverMessage basically it's name of friend
     * */
    public void receiveFriendRequest(String serverMessage)
    {
        notifications.receiveFriendRequest(serverMessage);
    }
    /**
     * Alerts notification panel about new group invitation
     * @param serverMessage name of group
     * */
    public void receiveGroupInvitation(String serverMessage)
    {
        notifications.receiveGroupInvitation(serverMessage);
    }


    /**In case friends or groups have been added
     * refresh lists, shown on mainTab */
    public void refresh()
    {
        panel.refreshFriends();
        panel.refreshGroups();
    }


    /**
     * plays a nice sound informing that there's a new message
     */
    public void newMessageSound()
    {

        try {
            File f = new File(pathToNewMessageAlert);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e)
        {
            //better do nothing, it was not needed that much anyways
        }
    }

    /**
     * plays a nice sound informing that there's a friend/group request
     */
    public void newRequestSound()
    {

        try {
            File f = new File(pathToRequestAlert);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f.toURI().toURL());
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        }
        catch (Exception e)
        {
            //better do nothing, it was not needed that much anyways
        }
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
