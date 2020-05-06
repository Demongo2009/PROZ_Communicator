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


    public ApplicationClient() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
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




                frame.pack();
                frame.setVisible(true);



            }
        });



    }
}
*/