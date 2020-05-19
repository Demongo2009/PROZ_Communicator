package Client.GUI.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateNewGroupDialog extends JDialog
{
    private JPanel mainPanel = new JPanel();
    private JButton createButton  = new JButton("CREATE");
    private JList FriendList;
    private JScrollPane FriendScroll;
    private JTextField GroupNameField = new JTextField(20);
    String GroupName="";

    /*Chyba nie da sie łatwo zrobic max 4 zaznaczen w JLiscie
     * Przyjmijmy, że użytkownik tyle nie zaznaczy*/
    CreateNewGroupDialog(JFrame parent, DefaultListModel friends)
    {
        super(parent,"CreateNewGroup",true);
        FriendList = new JList(friends);
        FriendScroll = new JScrollPane(FriendList);
        createButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int[] selected;
                selected = FriendList.getSelectedIndices();



                dispose();
            }
        });
        setLocationRelativeTo(null);
        mainPanel.setLayout(new FlowLayout());
        mainPanel.add(FriendScroll);
        mainPanel.add(GroupNameField);
        mainPanel.add(createButton);
        add(mainPanel);

    }



}
