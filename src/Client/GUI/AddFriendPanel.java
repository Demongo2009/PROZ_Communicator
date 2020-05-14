package Client.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class AddFriendPanel extends JPanel
{
    private String[] avaliablePeople = {"Igor","Konrad","Kuba","Bartek","Szymon","Twoja Stara XD","Odbyt","Dupa","Gowno","Ruchadło leśne"};
    private DefaultListModel lItems = new DefaultListModel();
    private JList lyst = new JList(lItems);
    private JScrollPane lst = new JScrollPane(lyst);
    private JButton b = new JButton("Add Friend");
    //private BufferedImage image;
    //JLabel picLabel;

    public AddFriendPanel()
    {


        //picLabel = new JLabel((new ImageIcon("addFriend.gif")));
        //meme = new ImageIcon(getClass().getResource("addFriend.gif"));
        setLayout(null);
        Border brd = BorderFactory.createMatteBorder(
                1, 1, 2, 2, Color.BLACK);
        lst.setBorder(brd);


        for(Object d :avaliablePeople)
        {
            lItems.addElement(d);
        }
        lst.setBounds(30,30,150,20*avaliablePeople.length>300?20*avaliablePeople.length:300);
        b.setBounds(350,450,170,30);
        //picLabel.setBounds(200,30,500,500);

        add(lst);
        add(b);
        //add(picLabel);
        // Register event listeners

    }


}
