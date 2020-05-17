package Client.GUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Client.Client.friends;

public class MainTab extends JPanel
{
    private DefaultListModel lItems = new DefaultListModel();
    private JList lyst = new JList(lItems);
    private JScrollPane lst = new JScrollPane(lyst);
    private JPanel AddFriend;
    private JPanel notificationPanel;
    private MainWindow referenceToMain;
    private JLabel state = new JLabel("");
    private JLabel loggedAs = new JLabel("");
    private JButton b = new JButton("Start Chat");
    String []Friends = {"Igor","Konrad","Kuba","Bartek","Szymon","Twoja Stara XD","Ruchadło leśne", "Dupa","Odbyt XD"};


    public MainTab(MainWindow upRef, String User)
    {
        referenceToMain=upRef;
        AddFriend = new AddFriendPanel(upRef);
        notificationPanel = new NotoficationPanel();
        setLayout(null);

        loggedAs.setText("ZALOGOWANO JAKO:  "+User);


        Border brd = BorderFactory.createMatteBorder(
                1, 1, 2, 2, Color.BLACK);
        lst.setBorder(brd);
        //LISTA PRZYJACIOL
        for(Object d :Friends)
        {
            lItems.addElement(d);
        }

//        friends.add("Igor");
//        friends.add("ddd ");
//        friends.add("dddf");
//        friends.add("ggggg");
//
//        for(String s: friends)
//        {
//            System.out.print("PRZYJACIEL: ");
//            System.out.println(s);
//            lItems.addElement(s);
//        }
        //USTAWIANIE ELEMENTOW
       // lst.setBounds(30,30,150,20*friends.size()<400?20*friends.size():400);

        lst.setBounds(30,30,150,20*Friends.length<400?20*Friends.length:400);

        state.setBounds(335,410,200,20);
        loggedAs.setBounds(355,30,250,70);
        b.setBounds(350,450,170,30);
        AddFriend.setBounds(30,370,155,110);
        state.setForeground(Color.RED);
        //AKCJE PRZYCISKOW
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                int []tab;
                if(lyst.isSelectionEmpty())
                {
                    state.setText("Select at least one friend first!");
                }
                tab= lyst.getSelectedIndices();
                if(tab.length==1)
                {
                    state.setText("");
                    //System.out.println(lyst.getSelectedValue().toString());
                    if(referenceToMain.OpenChatWindow(lyst.getSelectedValue().toString()))
                    {
                        state.setText("Rozmowa z tym userem juz trwa!");
                    }

                    //tabs.addTab("CZAT", new ChatWindow(Username));
                }
                //rozmowa grupowa - utworzenie
                else
                {

                }
            }
        });
        //WSTAWIANIE DO PANELU
        add(AddFriend);
        add(loggedAs);
        add(state);
        add(lst);
        add(b);

    }
}
