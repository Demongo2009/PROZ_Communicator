package Client.GUI.Notifications;

import Client.GUI.Main.MainWindow;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Client.Client.confirmFriendship;

public class NotificationPanel extends JPanel
{
    MainWindow referenceToMainWindow;
    /*Friends Requests*/
    private DefaultListModel friendsRequestsList = new DefaultListModel();
    private JList friendRequestL = new JList(friendsRequestsList);
    private JScrollPane friendRequestScroll = new JScrollPane(friendRequestL);
    private JButton acceptFriendRequest = new JButton("Accept");
    private JButton declineFriendRequest = new JButton("Decline");
    private JLabel friendRequestsText = new JLabel("Friendship requests:");

    /*Added to group Notifications*/
    //private DefaultListModel



    public NotificationPanel(MainWindow upRef)
    {
        referenceToMainWindow=upRef;
        setLayout(null);
        friendRequestsText.setBounds(10,10,200,20);
        friendRequestScroll.setBounds(10,40,200,180);
        declineFriendRequest.setBounds(15,230,90,20);
        acceptFriendRequest.setBounds(115,230,90,20);

        acceptFriendRequest.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(friendRequestL.isSelectionEmpty()) {}
                else
                {
                    confirmFriendship(friendRequestL.getSelectedValue().toString());
                    friendsRequestsList.removeElementAt(friendsRequestsList.indexOf(friendRequestL.getSelectedValue().toString()));
                    referenceToMainWindow.refresh();
                }
            }
        });
        declineFriendRequest.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                friendsRequestsList.removeElementAt(friendsRequestsList.indexOf(friendRequestL.getSelectedValue().toString()));
            }
        });


        add(friendRequestScroll);
        add(acceptFriendRequest);
        add(declineFriendRequest);
    }


    public void receiveFriendRequest(String serverMessage)
    {
        if(friendsRequestsList.indexOf(serverMessage)==-1)
            friendsRequestsList.addElement(serverMessage);

    }




}
