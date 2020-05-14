package Client.GUI;
import static Client.GUI.tools.SwingConsole.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javafx.util.Pair;

import static Client.GUI.tools.SwingConsole.*;
import javax.swing.*;

public class StartingScreen extends JFrame
{
    private JPanel panel = new JPanel();
    private JButton loginButton = new JButton("LOGIN");
    private JButton registerButton = new JButton("REGISTER");
    private JLabel OperationState =  new JLabel("");
    private String login="";
    private String pass="";




    private void CheckLoginPassword() throws IOException {
        if(login.equals("Igor") && pass.equals("dupa"))
        {
            OperationState.setText("LOGOWANIE SIE UDALO :)");
            run(new MainWindow(login),"KOMUNIKATOR XD",800,700);
            //run(new ChatWindow(login),500,650);
            dispose();
        }
        else
        {
            OperationState.setForeground(Color.RED);
            OperationState.setText("LOGOWANIE NIE POWIODLO SIE");
        }
    }
    private void CheckRegister()
    {
        if(!login.isEmpty())
            OperationState.setText("DODANO UZYTKOWNIKA: "+login);
    }

    public StartingScreen()
    {
        setTitle("CHOOSE LOGIN OR REGISTER");
        panel.setLayout(new FlowLayout());
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LoginScreenDialog dlg = new LoginScreenDialog(null);
                dlg.setSize(300,180);
                dlg.setVisible(true);
                login = dlg.getLogin();
                pass = dlg.getPassword();
                try {
                    CheckLoginPassword();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                RegisterScreenDialog dlg = new RegisterScreenDialog(null);
                dlg.setSize(300,250);
                dlg.setVisible(true);
                login = dlg.getLogin();
                pass = dlg.getPassword();
                CheckRegister();
            }
        });
        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(OperationState);
        add(panel);
    }

    public static void main(String[] args) throws IOException {
        //run(new ChatWindow("dupek XD"),500,650);
        //run(new StartingScreen(),"KOMUNIKATOR",300,100);
        run(new MainWindow("IGOR"),"XDDDDD",600,600);
    }

}
