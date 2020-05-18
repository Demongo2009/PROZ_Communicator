package Client.GUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Client.Client.addUserToFriends;

public class AddFriendPanel extends JPanel
{

    private JButton b = new JButton("Add Friend");
    private JPanel AddFriendArea;
    private JTextField FriendNameInput = new JTextField(20);
    private JLabel MainTabLabel;
    private JLabel AddFriendText = new JLabel("Enter user name:");



    public AddFriendPanel(JLabel upRef)
    {
        MainTabLabel = upRef;
        setLayout(null);
        AddFriendText.setBounds(10,20,135,30);
        b.setBounds(50,90,95,30);
        FriendNameInput.setBounds(10,50,135,30);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(addUserToFriends(FriendNameInput.getText())==-1)
                {
                    MainTabLabel.setText("<html>User is already your friend!</html>");
                }

            }
        });

        AddFriendArea = new JPanel();
        AddFriendArea.setBorder(new TitledBorder("Add Friend"));
        AddFriendArea.setLayout(null);
        AddFriendArea.add(FriendNameInput);
        AddFriendArea.add(b);
        AddFriendArea.setSize(155,140);
        AddFriendArea.add(AddFriendText);
        add(AddFriendArea);


    }


}