package Client.GUI.Chat;

import Client.GUI.Main.MainWindow;
import Client.GUI.tools.LabelTextBubbleBorder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Client.Client.sendTextMessageToGroup;
import static Client.GUI.tools.ImageScaler.resizeImage;


public class GroupChatWindow extends JPanel
{
    private final int   CHAT_BOX_WIDTH = 200;
    private final int   WIDTH_MULTIPLIER = 7; /** Since letters don't have fixed width, their count is multiplied */
private JPanel      mainPanel = new JPanel();
    private JPanel      southPanel = new JPanel();
    private JPanel      buttonsPanel = new JPanel();
    private JTextField  messageBox = new JTextField(30);
    JButton             sendButton = new JButton("Send Message");
    JButton             closeButton = new JButton("Close chat");
    JButton             addToGroupButton = new JButton("Add friend to group");
    String              username;
    String              groupName;
    String              URLToImage="";
    MainWindow          upRef;
    private JPanel      chatBox;
    JScrollPane         scrollPane;
    Date                date;
    SimpleDateFormat    formatter = new SimpleDateFormat("HH:mm:ss");

    /** Regex that extracts URL to the image from the message - modified RFC 2396*/
    String URLRegex = "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|gif|png|jpeg|JPG))(?:\\?([^#]*))?(?:#(.*))?";

    /**
     * prepares message
     * */
    private void TryToSend()
    {
        if(messageBox.getText().length()<1)
        {
            //nic
        }
        else
        {
            String message = messageBox.getText();
            if (containsImage(message))
            {

                String []notURL=message.split(URLRegex);

                String rest="";
                if(notURL.length!=0)
                {
                    if (notURL.length>0)
                        rest += notURL[0];
                    if (notURL.length != 1)
                        rest += notURL[1];
                }

                addRightChatPicture(URLToImage,rest);
            }
            else
                addRightChat(message,false);

            sendTextMessageToGroup(groupName,message);
            messageBox.setText("");
        }
    }

    /**
     * write down received message (on chat screen)
     * @param messageText content of message
     * @param sender from whom message
     */
    public void receiveMessage(String messageText,String sender)
    {

        if (containsImage(messageText))
        {
            String []notURL=messageText.split(URLRegex);

            String rest="";
            if(notURL.length!=0)
            {
                if (notURL.length>0) {
                    rest += notURL[0];
                }
                if (notURL.length > 1) {
                    rest += notURL[1];
                }
            }

            addLeftChatPicture(sender,URLToImage,"<b>"+sender+ "</b>: " + rest);
        }
        else
            addLeftChat("<b>"+sender+ "</b>: " + messageText,sender,false);
    }

    /**
     * Set URLToImage based on whether received
     * or sent message contains such URL
     * @param  messageText content of message
     * @return {@code true} if messageText contains picture URl */
    boolean containsImage(String messageText)
    {
        String msgLink=messageText;
        if(messageText.contains("http"))
        {
            msgLink = messageText.substring(messageText.indexOf("http"));
        }
        Matcher m = Pattern.compile(URLRegex).matcher(msgLink);
        if (m.find())
        {
            URLToImage=m.group(0);
            return true;
        }
        return false;
    }

    private class sendMessageButtonListener implements ActionListener
    {
        public void actionPerformed(ActionEvent event)
        {
            TryToSend();
        }
    }

    public GroupChatWindow(String login, String groupName, MainWindow upRef)
    {
        this.upRef = upRef;
        this.groupName= groupName;
        username = login;
        setLayout(new BorderLayout());
        mainPanel.setLayout(new BorderLayout());
        southPanel.setBackground(Color.PINK);
        southPanel.setLayout(new GridBagLayout());
        messageBox.requestFocusInWindow();
        sendButton.addActionListener(new GroupChatWindow.sendMessageButtonListener());
        chatBox = new JPanel();
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                upRef.CloseGroupChatWindow(groupName);
            }
        });
        addToGroupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
        addToGroupButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                AddToGroupDialog dlg = new AddToGroupDialog(groupName);
                dlg.setVisible(true);
                dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            }
        });
        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(chatBox);
        mainPanel.add(scrollPane, BorderLayout.CENTER);


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
        southPanel.add(sendButton, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);
        buttonsPanel.setLayout(new BorderLayout());
        buttonsPanel.add(BorderLayout.CENTER,closeButton);
        buttonsPanel.add(BorderLayout.EAST,addToGroupButton);
        mainPanel.add(BorderLayout.NORTH, buttonsPanel);
        add(mainPanel);
    }

    /**
     *
     * @param flowLayoutAlign TRAILING or LEADING - right or left side of chat
     * @param borderColor color of background of a message
     * @param messageContent printed text
     * @param isPicture if message contains URL to picture
     * @throws IOException when exception time comes exception is thrown
     */
    private void addChat(int flowLayoutAlign, Color borderColor,String messageContent,boolean isPicture)
            throws IOException
    {
        JPanel panel = new JPanel(new FlowLayout(flowLayoutAlign));
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createLineBorder(borderColor));
        AbstractBorder brdr = new LabelTextBubbleBorder(Color.BLACK,2,16,0);
        label.setBorder(brdr);
        label.setBackground(borderColor);
        label.setOpaque(true);

        int chatBoxWidth =
                messageContent.length()*WIDTH_MULTIPLIER<CHAT_BOX_WIDTH?
                        messageContent.length()*WIDTH_MULTIPLIER:
                        CHAT_BOX_WIDTH;


        //label.setText("<html><body style='width: " + chatBoxWidth + "px; word-break:break-all;'>" + messageContent);

        if(isPicture)
        {
            BufferedImage image;
            URL link = new URL(messageContent);
            image = ImageIO.read(link);
            image.getWidth();
            image = resizeImage(image,new Dimension(400,400));
            label.setIcon(new ImageIcon(image));
        }

        else
        {
            if(messageContent.equals(""))
                return;
            label.setText("<html><body style='width: " + chatBoxWidth + "px; word-break:break-all;'>" + messageContent);
        }


        date=new Date();
        label.setToolTipText(username+" "+formatter.format(date));

        panel.add(label);
        chatBox.add(panel);
        chatBox.revalidate();
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
    }

    /**
     *
     * @param flowLayoutAlign TRAILING or LEADING - right or left side of chat
     * @param borderColor color of background of a message
     * @param messageContent printed text
     * @param sender user, who sent a message
     * @param isPicture if message contains URL to picture
     * @throws IOException when exception time comes exception is thrown
     */
    private void addChat(int flowLayoutAlign, Color borderColor,String messageContent,String sender,boolean isPicture)
            throws IOException
    {
        JPanel panel = new JPanel(new FlowLayout(flowLayoutAlign));
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createLineBorder(borderColor));
        AbstractBorder brdr = new LabelTextBubbleBorder(Color.BLACK,2,16,0);
        label.setBorder(brdr);
        label.setBackground(borderColor);
        label.setOpaque(true);

        int chatBoxWidth =
                messageContent.length()*WIDTH_MULTIPLIER<CHAT_BOX_WIDTH?
                        messageContent.length()*WIDTH_MULTIPLIER:
                        CHAT_BOX_WIDTH;

        if(isPicture)
        {
            BufferedImage image;
            URL link = new URL(messageContent);
            image = ImageIO.read(link);
            image.getWidth();
            image = resizeImage(image,new Dimension(400,400));
            label.setIcon(new ImageIcon(image));
        }

        else
        {
            if(messageContent.equals(""))
                return;
            label.setText("<html><body style='width: " + chatBoxWidth + "px; word-break:break-all;'>" + messageContent);
        }
        //label.setText("<html><body style='width: " + chatBoxWidth + "px; word-break:break-all;'>" + messageContent);

        date=new Date();
        label.setToolTipText(sender+" "+formatter.format(date));
        panel.add(label);
        chatBox.add(panel);
        chatBox.revalidate();
//        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
//        SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
    }

    /**
     * adding chat message from user
     * @param messageContent content of received message (can be anything literally, even love confession)
     * @param  isPicture true when message contains picture
     *
     * */
    private void addRightChat(String messageContent,boolean isPicture)
    {
        try {
            addChat(FlowLayout.TRAILING, Color.pink,messageContent,isPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * adding chat message from the talker
     * @param messageContent content of received message (can be anything literally, even love confession)
     * @param sender who sent message
     * @param  isPicture true when message contains picture
     * */
    private void addLeftChat(String messageContent,String sender,boolean isPicture)
    {
        try {
            addChat(FlowLayout.LEADING, Color.CYAN,messageContent,sender,isPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * adding picture on chat from user
     * @param pictureURL URL to image
     * @param restOfMessage remaining text (if there is)
     * */
    private void addRightChatPicture(String pictureURL,String restOfMessage)
    {
        addRightChat(restOfMessage,false);
        addRightChat(pictureURL,true);


    }
    /**
     * adding picture on chat from the talker
     * @param sender who sent message
     * @param  pictureURL URL to picture
     * @param  restOfMessage remaining text (if there is)
     * */
    private void addLeftChatPicture(String sender,String pictureURL,String restOfMessage)
    {
        addLeftChat(restOfMessage,sender,false);
        addLeftChat(pictureURL,sender,true);

    }

}


