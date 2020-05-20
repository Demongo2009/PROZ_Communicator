package Client.GUI.Chat;

import Client.Client;
import Client.GUI.Main.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddToGroupDialog extends JDialog
{
    private JPanel mainPanel = new JPanel();
    private DefaultListModel friendsList = new DefaultListModel();
    private JList friendL = new JList(friendsList);
    private JScrollPane friendScroll = new JScrollPane(friendL);
    private JButton addFriendToGroupButton = new JButton("Add Friend");
    private JButton cancelButton = new JButton("Close");
    private JLabel state = new JLabel("");

    public AddToGroupDialog(String groupName)
    {
        setLayout(null);
        setLocationRelativeTo(null);
        for(String s: Client.friends)
        {
            friendsList.addElement(s);
        }

        friendL.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        addFriendToGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!friendL.isSelectionEmpty())
                {
                    try
                    {
                        Client.addUserToGroup(groupName,friendL.getSelectedValue().toString());
                        state.setText("Added to group!");
                    }
                    catch (Exception exc)
                    {
                           state.setText(exc.getMessage());
                    }
                }

            }
        });
        setTitle("Add to "+ groupName);
        setSize(250,360);
        friendScroll.setBounds(10,10,150,150);
        state.setBounds(10,180,200,30);
        cancelButton.setBounds(10,230,100,30);
        addFriendToGroupButton.setBounds(120,230,100,30);

        add(friendScroll);
        add(state);
        add(cancelButton);
        add(addFriendToGroupButton);


    }

}
