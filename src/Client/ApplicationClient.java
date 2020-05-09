package Client;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Semaphore;
/*
public class ApplicationClient {

    private JTabbedPane conversations;
    private JTextField inputField;
    private JTextArea conversationText;
    private JPanel conversationPanel;
    private JFrame frame;
    private JFrame loginFrame;


    public ApplicationClient() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                loginFrame = new JFrame("Login");
                loginFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                loginFrame.setLayout(new GridLayout(3,1));

                JLabel loginLabel = new JLabel("Please log in or sign in.");
                JButton loginButton = new JButton("log in");
                JButton signInButton = new JButton("sign in");
                loginFrame.add(loginLabel);
                loginFrame.add(loginButton);
                loginFrame.add(signInButton);

                JFrame loggingInFrame = new JFrame("Logging in");
                loggingInFrame.setLayout(new GridLayout(4,1));
                loggingInFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JLabel loggingInLabel = new JLabel("Please input login and password");
                JTextField loginTextField = new JTextField();
                JTextField passwordLoginTextField = new JTextField();
                JButton saveLoginButton = new JButton("Save");
                loggingInFrame.add(loggingInLabel);
                loggingInFrame.add(loginTextField);
                loggingInFrame.add(passwordLoginTextField);
                loggingInFrame.add(saveLoginButton);



                saveLoginButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        String login = loginTextField.getText();
                        String password = passwordLoginTextField.getText();


                        Client.sendFromApp(login+" "+password);

                        loggingInFrame.setVisible(false);


                        frame.pack();
                        frame.setVisible(true);
                    }
                });

                loginButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Client.sendFromApp("/");

                        loginFrame.setVisible(false);

                        loggingInFrame.pack();
                        loggingInFrame.setVisible(true);
                    }
                });

                JFrame signInFrame = new JFrame();
                signInFrame.setLayout(new GridLayout(4,1));
                signInFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                JLabel signInLabel = new JLabel("Please input login and password");
                JTextField loginSignInTextField =  new JTextField();
                JTextField passwordSignInTextField = new JTextField();
                JButton saveSignInButton = new JButton("Save");
                signInFrame.add(signInLabel);
                signInFrame.add(loginSignInTextField);
                signInFrame.add(passwordSignInTextField);
                signInFrame.add(saveSignInButton);

                //TODO: bolean for indicatoin if login was successful

                saveSignInButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        String login = loginTextField.getText();
                        String password = passwordLoginTextField.getText();


                        Client.sendFromApp(login+" "+password);

                        signInFrame.setVisible(false);


                        frame.pack();
                        frame.setVisible(true);
                    }
                });

                signInButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Client.sendFromApp(".");

                        loginFrame.setVisible(false);

                        signInFrame.pack();
                        signInFrame.setVisible(true);
                    }
                });

                frame = new JFrame("MultiCom");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setPreferredSize(new Dimension(500,500));


                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.setTabPlacement(SwingConstants.LEFT);
                conversations = tabbedPane;

                JPanel tabPanel = new JPanel();
                conversations.add(tabPanel);

                JTextField textField = new JTextField();
                textField.setPreferredSize(new Dimension(300,50));
                inputField = textField;

                JTextArea textArea= new JTextArea();
                textArea.setPreferredSize(new Dimension(300,300));
                textArea.setEditable(false);

                conversationText = textArea;

                tabPanel.add(conversationText);
                tabPanel.add(inputField);
                tabbedPane.setTitleAt(0,"Conversation1");


                JPanel tabPanel1 =new JPanel();
                JTextField textField1= new JTextField();
                textField1.setPreferredSize(new Dimension(300,50));


                JTextArea textArea1 = new JTextArea();
                textArea1.setPreferredSize(new Dimension(300,300));
                textArea1.setEditable(false);

                tabPanel1.add(textArea1);
                tabPanel1.add(textField1);

                tabbedPane.addTab("Conversation2",tabPanel1);

//        frame.setLayout(new Lay);
                frame.add(tabbedPane);



                inputField.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Client.sendFromApp(inputField.getText());
                        inputField.setText("");
                    }
                });
                textField1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        Client.sendFromApp(inputField.getText());
                        inputField.setText("");
                    }
                });
            }
        });

    }

//    public class Worker extends SwingWorker<String, String> {
//        JTextArea conText;
//        String text;
//
//        public Worker(JTextArea conText,String text){
//            this.conText =conText;
//            this.text = text;
//        }
//        @Override
//        protected String doInBackground() throws Exception {
//
//            publish(text);
//            System.out.println(text);
////                Thread.sleep(1);
//            return null;
//        }
//
//        @Override
//        protected void process(List<String> chunks) {
//            for (String chunk : chunks) {
////                conText.setEditable(true);
//                System.out.println("f");
//                conText.append(chunk + "\n");
//            }
//        }
//
//        @Override
//        protected void done() {
//
//            conText.append("s\n");
//        }
//
//    };

    public void updateConversationText(String text) {
//        conversationText.append(text);
//        conversationText.setText(text);
//        System.out.println(text);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                conversationText.append(text+"\n");
            }
        });



    }


    public void show(){
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
//                JFrame frame = new JFrame("ApplicationClient");
//                frame.setContentPane(new ApplicationClient().mulltiCom);
//                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//                frame.pack();
//                frame.setVisible(true);




                loginFrame.pack();
                loginFrame.setVisible(true);



            }
        });



    }
}
*/