package Client.GUI;

import javax.swing.*;
import java.awt.*;

public class MainWindow extends JFrame
{
    private JPanel panel = new JPanel();
    private JLabel are = new JLabel("DUPA",SwingConstants.CENTER);



    public MainWindow()
    {
        setTitle("KOMUNIKATOR XD");
        panel.setLayout(new BorderLayout());
        panel.add(are,BorderLayout.CENTER);
        add(panel);
    }


}
