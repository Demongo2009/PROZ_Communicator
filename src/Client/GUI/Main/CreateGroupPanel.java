package Client.GUI.Main;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static Client.Client.addUserToFriends;
import static Client.Client.createGroup;

public class CreateGroupPanel extends JPanel
{
    private JButton b = new JButton("Create Group");
    private JPanel createGroupArea;
    private JTextField groupNameInput = new JTextField(20);
    private JLabel MainTabLabel;
    private JLabel createGroupText = new JLabel("Enter group name:");
    private MainWindow referenceToMain;

    void submit()
    {
        try
        {
            createGroup(groupNameInput.getText());
        }
        catch (Exception exc)
        {
            MainTabLabel.setForeground(Color.RED);
            MainTabLabel.setText("<html>"+exc.getMessage()+"</html>");
        }
        referenceToMain.refresh();
    }

    public CreateGroupPanel(JLabel upRefText,MainWindow upRef)
    {
        referenceToMain=upRef;
        MainTabLabel = upRefText;
        setLayout(null);
        createGroupText.setBounds(10,20,135,30);
        b.setBounds(10,90,135,30);
        groupNameInput.setBounds(10,50,135,30);

        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                submit();
            }
        });
        groupNameInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode()==KeyEvent.VK_ENTER)
                    submit();
            }
        });

        createGroupArea = new JPanel();
        createGroupArea.setBorder(new TitledBorder("Create group"));
        createGroupArea.setLayout(null);
        createGroupArea.add(groupNameInput);
        createGroupArea.add(b);
        createGroupArea.setSize(155,140);
        createGroupArea.add(createGroupText);
        add(createGroupArea);


    }
}
