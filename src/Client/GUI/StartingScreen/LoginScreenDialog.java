package Client.GUI.StartingScreen;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginScreenDialog extends JDialog
{
    private final int WIDTH=110;
    private final int HEIGHT=25;
    private final int BUTTON_WIDTH =100;
    private JPanel panel = new JPanel();
    private JButton login_button = new JButton("LOGIN");
    private JButton close_button = new JButton("CLOSE");
    private JLabel userText = new JLabel("User");
    private JTextField userName = new JTextField(20);
    private JLabel passwordText = new JLabel("Password");
    private JPasswordField userPassword = new JPasswordField(20);
    String login = "";
    String password = "";

    public LoginScreenDialog(JFrame parent)
    {
        super(parent, "Login Screen", true);
        setLocationRelativeTo(null);
        login_button.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                login = userName.getText();
                //System.out.println("LOGIN TO: " + login);
                password = userPassword.getText();
                //System.out.println("HASLO TO: " + password);
                dispose();
            }
        });

        close_button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose(); // Closes the dialog
            }
        });

        userPassword.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    login = userName.getText();
                    password = userPassword.getText();
                    dispose();
                }

            }
        });

        userName.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if(e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    login = userName.getText();
                    password = userPassword.getText();
                    dispose();
                }

            }
        });
        userText.setBounds(10,20,WIDTH,HEIGHT);
        passwordText.setBounds(10,50,WIDTH,HEIGHT);
        userName.setBounds(100,20,WIDTH,HEIGHT);
        userPassword.setBounds(100,50,WIDTH,HEIGHT);
        login_button.setBounds(130,100,BUTTON_WIDTH,HEIGHT);
        close_button.setBounds(10,100,BUTTON_WIDTH,HEIGHT);
        panel.setLayout(null);
        panel.add(userName);
        panel.add(userText);
        panel.add(userPassword);
        panel.add(passwordText);
        panel.add(login_button);
        panel.add(close_button);
        add(panel);
        userName.requestFocus();

    }

    /**
     *
     * @return login :-)
     */
    String getLogin()
    {return  login;}

    /**
     *
     * @return password :)
     */
    String getPassword()
    {return  password;}


}