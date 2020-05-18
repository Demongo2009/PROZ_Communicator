package Client.GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddToGroupDialog extends JDialog
{
    private JButton SendRequest = new JButton("Send Request");
    private JButton CloseButton = new JButton("Close");
    private JList GroupList;
    private JLabel state = new JLabel("");
    private  JScrollPane GroupScroll;
    private JPanel mainPanel = new JPanel();
    private JPanel SouthPanel = new JPanel();


    AddToGroupDialog(DefaultListModel frndlst,String addingFriend)
    {
        setLayout(new FlowLayout());
        state.setSize(200,30);
        GroupList = new JList(frndlst);
        GroupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        GroupScroll =  new JScrollPane(GroupList);
        //GroupScroll.setSize(200,100);
        mainPanel.add(GroupScroll,BorderLayout.CENTER);


        SendRequest.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(GroupList.isSelectionEmpty())
                {
                    state.setText("Select a group!");
                }
                else
                {
                    dispose();
                }
            }
        });
        CloseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        //SouthPanel.setLayout(new BorderLayout());
        add(mainPanel);
        add(state);
        SouthPanel.add(CloseButton,BorderLayout.WEST);
        SouthPanel.add(SendRequest,BorderLayout.EAST);
        add(SouthPanel);
        setTitle(addingFriend);
    }




}
