package Client.GUI;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class AddFriendPanel extends JPanel
{
    private String[] avaliablePeople = {"Igor","Konrad","Kuba","Bartek","Szymon","Twoja Stara XD","NIE PYTAJĄC SIE O IMIE WALCZA Z OSTRYM CIENIEM MGLY"};
    private DefaultListModel lItems = new DefaultListModel();
    private JList lyst = new JList(lItems);
    private JScrollPane lst = new JScrollPane(lyst);
    private JButton b = new JButton("Add Friend");
    private BufferedImage image;
    JLabel picLabel;

    public AddFriendPanel() throws IOException {

        String path = "https://cdn.natemat.pl/5c6979ac9be18ad6c14f773fc606b87d,382,0,0,0.jpg";
        URL link = new URL(path);
        image = ImageIO.read(link);
        picLabel = new JLabel(new ImageIcon(image));
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
        //lst.setBounds(30,30,150,400);
        System.out.println("TYLE JEST: " + avaliablePeople.length);
        lst.setBounds(30,30,150,22*avaliablePeople.length<400?22*avaliablePeople.length:400);
        b.setBounds(350,450,170,30);
        picLabel.setBounds(200,30,500,500);

        add(lst);
        add(b);
        add(picLabel);
        // Register event listeners

    }


}
