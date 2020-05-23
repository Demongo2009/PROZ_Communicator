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

import static Client.Client.sendTextMessageToUser;
import static Client.GUI.tools.ImageScaler.resizeImage;


public class ChatWindow extends JPanel
{
    private final int CHAT_BOX_WIDTH = 200;
    private final int WIDTH_MULTIPLIER = 7;
    private JPanel mainPanel = new JPanel();
    private JPanel southPanel = new JPanel();
    private JTextField messageBox = new JTextField(30);
    JButton sendButton = new JButton("Send Message");
    JButton closeButton = new JButton("Close chat");
    String username;
    String receiver;
    String URLToImage="";
    MainWindow upRef;
    private JPanel chatBox;
    JScrollPane scrollPane;
    Date date = new Date();
    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
    String URLRegex = "(?:([^:/?#]+):)?(?://([^/?#]*))?([^?#]*\\.(?:jpg|gif|png|jpeg|JPG))(?:\\?([^#]*))?(?:#(.*))?";
    //String URLRegex = "^https?://(?:[a-z0-9\\-]+\\.)+[a-z]{2,6}(?:/[^/#?]+)+\\.(?:jpg|gif|png|JPG|jpeg)$";

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
                String[] notURL = message.split(URLRegex);

                String rest = "";
                if (notURL.length != 0) {
                    if (notURL.length > 0)
                        rest += notURL[0];
                    if (notURL.length != 1)
                        rest += notURL[1];
                }
                if (URLToImage.startsWith("http")) {
                    addRightChatPicture(URLToImage, rest);
                }
                else
                {
                    addRightChat(message,false);

                }
            }
            else
                addRightChat(message,false);

            sendTextMessageToUser(receiver,message);
            messageBox.setText("");
        }
    }

    public void receiveMessage(String messageText)
    {
        if (containsImage(messageText))
        {
            String []notURL=messageText.split(URLRegex);

            String rest="";
            if(notURL.length!=0)
            {
                if (notURL.length>0)
                    rest += notURL[0];
                if (notURL.length != 1)
                    rest += notURL[1];
            }
            if (URLToImage.startsWith("http"))
            {
                addLeftChatPicture(URLToImage,rest);
            }
            else
            {
                addLeftChat(messageText,false);
            }
        }
        else
            addLeftChat(messageText,false);
    }
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
    public ChatWindow(String login, String friendName, MainWindow upRef)
    {
        this.upRef = upRef;
        receiver= friendName;
        username = login;
        setLayout(new BorderLayout());
        mainPanel.setLayout(new BorderLayout());
        southPanel.setBackground(Color.PINK);
        southPanel.setLayout(new GridBagLayout());
        messageBox.requestFocusInWindow();
        sendButton.addActionListener(new ChatWindow.sendMessageButtonListener());
        chatBox = new JPanel();

        chatBox.setLayout(new BoxLayout(chatBox, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(chatBox);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                upRef.CloseChatWindow(receiver);
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
        southPanel.add(sendButton, right);

        mainPanel.add(BorderLayout.SOUTH, southPanel);
        mainPanel.add(BorderLayout.NORTH, closeButton);
        add(mainPanel);
    }
    private void addChat(int flowLayoutAlign, Color borderColor,String messageContent,boolean isPicture) throws IOException
    {
        JPanel panel = new JPanel(new FlowLayout(flowLayoutAlign));
        JLabel label = new JLabel();
        label.setBorder(BorderFactory.createLineBorder(borderColor));
        AbstractBorder brdr = new LabelTextBubbleBorder(Color.BLACK,2,16,0);
        label.setBorder(brdr);
        label.setBackground(borderColor);
        label.setOpaque(true);

        int chatBoxWidth = messageContent.length()*WIDTH_MULTIPLIER<CHAT_BOX_WIDTH?messageContent.length()*WIDTH_MULTIPLIER:CHAT_BOX_WIDTH;
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

        if(flowLayoutAlign== FlowLayout.TRAILING)
        {
            date=new Date();
            label.setToolTipText(username+" "+formatter.format(date));
        }
        else
        {
            date=new Date();
            label.setToolTipText(receiver+" "+formatter.format(date));
        }

        panel.add(label);
        chatBox.add(panel);
        chatBox.revalidate();
        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
        SwingUtilities.invokeLater(() -> verticalScrollBar.setValue(verticalScrollBar.getMaximum()));
    }
    private void addRightChat(String messageContent,boolean isPicture)
    {
        try {
            addChat(FlowLayout.TRAILING, Color.pink,messageContent,isPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addLeftChat(String messageContent,boolean isPicture)
    {
        try {
            addChat(FlowLayout.LEADING, Color.CYAN,messageContent,isPicture);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addRightChatPicture(String pictureURL,String restOfMessage)
    {
        addRightChat(restOfMessage,false);
        addRightChat(pictureURL,true);


    }
    private void addLeftChatPicture(String pictureURL,String restOfMessage)
    {
        addLeftChat(restOfMessage,false);
        addLeftChat(pictureURL,true);

    }


}


