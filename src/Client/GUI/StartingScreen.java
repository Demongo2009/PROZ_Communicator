package Client.GUI;
import static Client.Client.*;
import static Client.GUI.tools.SwingConsole.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import Client.Client;
import Messages.clientToServer.ClientToServerMessageType;
import javafx.util.Pair;
import sun.applet.Main;

import static Client.GUI.tools.SwingConsole.*;
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

    private void CheckLoginPassword() throws IOException
    {
        /*
        *
        *
        *
        *
        *
        *
        TUTAJ JEST TRYB DZIALANIA
        * */

        if(true)
        {
            try
            {
                System.out.println("CHECKIGN LOGIN");
                sendLoginOrRegisterRequest(login, pass, ClientToServerMessageType.REQUEST_LOGIN);
                //TUTAJ MUSI BYC Receiveloginanswer do potwierdzenia ze sie udalo
                /*Tworzymy okno głownej aplikacji i wysyłamy referencje do niej do oblugi powiadomien
                 * sama aplikacja pokaze sie wtedy, gdy serwer zweryfikuje uzytkownika, w przeciwnym razie
                 * sproboj jeszcze raz*/
                MainWindow MainClientApp = new MainWindow(login);
                receiveLoginAnswer(MainClientApp);
                //wszysyko w porzadku? to odslon okno głowne
                OperationState.setText("Succesfully signed in :)");
                run(MainClientApp, STARTING_SCREEN_TITLE, 600, 600);
                //zamknij okno startowe
                dispose();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
        /*
        *Testowanie obu wariantow
        */
        if(false)
        {
            if (login.equals("Igor") && pass.equals("dupa")) {
                OperationState.setText("Logged in succesfully :)");
                run(new MainWindow(login), STARTING_SCREEN_TITLE, 600, 600);
                //run(new ChatWindow(login),500,650);
                dispose();
            } else {
                OperationState.setForeground(Color.RED);
                OperationState.setText("LOGOWANIE NIE POWIODLO SIE");
            }
        }
    }
    private void CheckRegister()
    {
        if(true)
        {
            try {
                System.out.println("CHECKIGN REGISTER");
                sendLoginOrRegisterRequest(login, pass, ClientToServerMessageType.REQUEST_REGISTER);
                //TUTAJ MUSI BYC Receiveloginanswer do potwierdzenia ze sie udalo
                /*Tworzymy okno głownej aplikacji i wysyłamy referencje do niej do oblugi powiadomien
                * sama aplikacja pokaze sie wtedy, gdy serwer zweryfikuje uzytkownika, w przeciwnym razie
                * sproboj jeszcze raz*/
                MainWindow MainClientApp = new MainWindow(login);
                receiveLoginAnswer(MainClientApp);
                //wszysyko w porzadku? to odslon okno głowne
                OperationState.setText("Succesfully signed up :)");
                run(MainClientApp, STARTING_SCREEN_TITLE, 600, 600);
                //zamknij okno startowe
                dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public StartingScreen( )
    {
        initClient();
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
        loginButton.requestFocus();
        getRootPane().setDefaultButton(loginButton);
    }

    public static void main(String[] args) throws IOException
    {
        //run(new StartingScreen(),"KOMUNIKATOR",300,100);
        run(new MainWindow("Igor"),"XDDDDD",600,600);
    }

}
