package Client.GUI;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainTab extends JPanel
{
    private DefaultListModel lItems = new DefaultListModel();
    private JList lyst = new JList(lItems);
    private JScrollPane lst = new JScrollPane(lyst);
    private JLabel state = new JLabel("");
    private JLabel loggedAs = new JLabel("");
    private JButton b = new JButton("Start Chat");
    String []Friends = {"Igor","Konrad","Kuba","Bartek","Szymon","Twoja Stara XD","Ruchadło leśne", "Dupa","Odbyt XD"};
    private MainWindow referenceToMain;

    public MainTab(MainWindow upRef, String User)
    {
        referenceToMain=upRef;
        loggedAs.setText("ZALOGOWANO JAKO:  "+User);
        setLayout(null);
        Border brd = BorderFactory.createMatteBorder(
                1, 1, 2, 2, Color.BLACK);
        lst.setBorder(brd);
        for(Object d :Friends)
        {
            lItems.addElement(d);
        }
        lst.setBounds(30,30,150,20*Friends.length<400?20*Friends.length:400);
        state.setBounds(30,450,200,20);
        loggedAs.setBounds(355,30,250,70);
        b.setBounds(350,450,170,30);
        state.setForeground(Color.RED);
        b.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(lyst.isSelectionEmpty())
                {
                    state.setText("NAJPIERW COS ZAZNACZ KOLES");
                }
                else
                {
                    state.setText("");
                    //System.out.println(lyst.getSelectedValue().toString());
                    if(referenceToMain.OpenChatWindow(lyst.getSelectedValue().toString()))
                    {
                        state.setText("Rozmowa z tym userem juz trwa!");
                    }

                    //tabs.addTab("CZAT", new ChatWindow(Username));
                }
            }
        });

        add(loggedAs);
        add(state);
        add(lst);
        add(b);

    }
}
