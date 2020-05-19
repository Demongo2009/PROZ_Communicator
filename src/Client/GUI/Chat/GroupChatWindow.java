package Client.GUI.Chat;

import Client.GUI.Main.MainWindow;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

import static Client.Client.sendTextMessageToGroup;


public class GroupChatWindow  extends JPanel
{
    private JPanel mainPanel = new JPanel();
    private JPanel southPanel = new JPanel();
    private JPanel buttonsPanel = new JPanel();
    private  JTextField  messageBox = new JTextField(30);
    JButton     sendMessage = new JButton("Send Message");
    JButton     closeButton = new JButton("Close chat");
    JButton     addToGroupButton = new JButton("Add friend to group");
    JTextArea   chatBox = new JTextArea();
    MainWindow upRef;
    String  username="Igor";
    String GroupName;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    public void receiveMessage(String messageText,String sender)
    {
        date = new Date();
        chatBox.append("<" + sender +" "+ formatter.format(date)+ ">:  " + messageText
                + "\n");
    }


    private void TryToSend()
    {
        if (messageBox.getText().length() < 1)
        {
            // do nothing
        }
        else if (messageBox.getText().equals(".clear"))
        {
            chatBox.setText("Cleared all messages\n");
            messageBox.setText("");
        }
        else
        {
            date = new Date();
            chatBox.append("<" + username +" "+ formatter.format(date)+ ">:  " + messageBox.getText()
                    + "\n");
            sendTextMessageToGroup(GroupName,messageBox.getText());
            messageBox.setText("");

        }
        messageBox.requestFocusInWindow();
    }

    //public ChatWindow(String login,String)
//KOnstruktor dla grupowego czatu
    public GroupChatWindow(String login,String GroupName, MainWindow upRef)

    {
        this.GroupName=GroupName;
        this.upRef = upRef;
        setLayout(new BorderLayout());
        username = login;
        mainPanel.setLayout(new BorderLayout());
        southPanel.setBackground(Color.PINK);
        southPanel.setLayout(new GridBagLayout());
        messageBox.requestFocusInWindow();
        chatBox.setEditable(false);
        chatBox.setFont(new Font("Serif", Font.PLAIN, 15));
        chatBox.setLineWrap(true);
        mainPanel.add(new JScrollPane(chatBox), BorderLayout.CENTER);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                upRef.CloseGroupChatWindow(GroupName);
            }
        });
        sendMessage.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TryToSend();
            }
        });
        addToGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        GridBagConstraints left = new GridBagConstraints();
        left.anchor = GridBagConstraints.LINE_START;
        left.fill = GridBagConstraints.HORIZONTAL;
        left.weightx = 512.0D;
        left.weighty = 1.0D;

        GridBagConstraints right = new GridBagConstraints();
        right.insets = new Insets(0, 10, 0, 0);
        right.anchor = GridBagConstraints.LINE_END;
        right.fill = GridBagConstraints.NONE;
        right.weightx = 1.0D;
        right.weighty = 1.0D;

        messageBox.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e) {
                super.keyPressed(e);
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TryToSend();
                }
            }
        });

        southPanel.add(messageBox, left);
        southPanel.add(sendMessage, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);

        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(BorderLayout.CENTER,closeButton);
        buttonsPanel.add(BorderLayout.EAST,addToGroupButton);
        mainPanel.add(BorderLayout.NORTH, buttonsPanel);
        add(mainPanel);
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(470, 300);
        setVisible(true);
    }
}
