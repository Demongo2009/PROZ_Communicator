package Client.GUI.StartingScreen;
import static Client.Client.*;
import static Client.GUI.tools.SwingConsole.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import Client.GUI.Main.MainWindow;
import Messages.clientToServer.ClientToServerMessageType;
//import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;

import javax.swing.*;

public class StartingScreen extends JFrame
{
    private String STARTING_SCREEN_TITLE = "Komunikator";
    private JPanel panel = new JPanel();
    private JButton loginButton = new JButton("LOGIN");
    private JButton registerButton = new JButton("REGISTER");
    private JLabel OperationState =  new JLabel("");
    private String login="";
    private String pass="";
    static String username = null;

    private boolean CheckLoginPassword()
    {
            boolean loginSuccesful=false;
            try
            {
                sendLoginOrRegisterRequest(login, pass, ClientToServerMessageType.REQUEST_LOGIN);

                if(receiveLoginAnswer())
                {
                    OperationState.setText("Succesfully signed in :)");
                    MainWindow MainClientApp = new MainWindow(login);
                    run(MainClientApp, STARTING_SCREEN_TITLE, 600, 600);
                    dispose();
                    loginSuccesful=true;
                }
                else
                {
                    OperationState.setForeground(Color.RED);
                    OperationState.setText("TRY AGAIN");
                    loginSuccesful=false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return loginSuccesful;

    }
    private void CheckRegister()
    {
            try
            {
                sendLoginOrRegisterRequest(login, pass, ClientToServerMessageType.REQUEST_REGISTER);
                MainWindow MainClientApp = new MainWindow(login);
                receiveLoginAnswer();
                OperationState.setText("Succesfully signed up");
                run(MainClientApp, STARTING_SCREEN_TITLE, 600, 600);
                dispose();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    }

    public StartingScreen( )
    {
        setTitle("CHOOSE LOGIN OR REGISTER");
        panel.setLayout(new FlowLayout());

        loginButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                LoginScreenDialog dlg = new LoginScreenDialog(null);
                dlg.setSize(300,180);
                dlg.setVisible(true);
                login = dlg.getLogin();
                pass = dlg.getPassword();
                CheckLoginPassword();
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
        loginButton.requestFocus();
        getRootPane().setDefaultButton(loginButton);
    }

}
