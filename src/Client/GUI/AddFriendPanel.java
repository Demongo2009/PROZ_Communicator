package Client.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AddFriendPanel extends JPanel
{

    private JButton b = new JButton("Add Friend");
    private JPanel AddFriendArea;
    private JTextField FriendNameInput = new JTextField(20);
    private MainWindow ReferenceToMain;



    public AddFriendPanel(MainWindow upRef)
    {
        ReferenceToMain = upRef;
        setLayout(null);

        b.setBounds(50,60,95,30);
        FriendNameInput.setBounds(10,20,135,30);

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {

            }
        });

        AddFriendArea = new JPanel();
        AddFriendArea.setBorder(new TitledBorder("Add Friend"));
        AddFriendArea.setLayout(null);
        AddFriendArea.add(FriendNameInput);
        AddFriendArea.add(b);
        AddFriendArea.setSize(155,110);
        add(AddFriendArea);


    }


}