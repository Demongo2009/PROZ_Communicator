package Client.GUI.Main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static Client.Client.addUserToFriends;

public class AddFriendPanel extends JPanel
{

    private JButton b = new JButton("Add Friend");
    private JPanel AddFriendArea;
    private JTextField FriendNameInput = new JTextField(20);
    private JLabel MainTabLabel;
    private JLabel AddFriendText = new JLabel("Enter user name:");

    /**Get user name form text field and send it (try to add this friend)*/
    private void submit()
    {
        if(FriendNameInput.getText().length()<3)
        {
            MainTabLabel.setForeground(Color.RED);
            MainTabLabel.setText("<html>Name too short!</html>");
        }
        else
        {
            if (!addUserToFriends(FriendNameInput.getText())) {
                MainTabLabel.setForeground(Color.RED);
                MainTabLabel.setText("<html>User is already your friend!</html>");
            } else {
                MainTabLabel.setForeground(Color.GREEN);
                MainTabLabel.setText("<html>Request sent!</html>");
            }
        }
    }

    public AddFriendPanel(JLabel upRefText)
    {
        MainTabLabel = upRefText;
        setLayout(null);
        AddFriendText.setBounds(10,20,135,30);
        b.setBounds(50,90,95,30);
        FriendNameInput.setBounds(10,50,135,30);

        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                submit();
            }
        });

        FriendNameInput.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode()==KeyEvent.VK_ENTER)
                    submit();
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
