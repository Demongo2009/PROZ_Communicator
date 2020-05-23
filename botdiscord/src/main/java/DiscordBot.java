import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;

import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageAttachment;

import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DiscordBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;
    static ObjectOutputStream outObject;
    static ObjectInputStream inObject;
    static String username;
    static String password;
    static Semaphore loginResultAvailable = new Semaphore(0);
    static boolean loginResult;
    static String friend;
    static boolean isGroupSending = false;

    // States of bot state machine
    enum AvailableStates{
        INIT,
        CONNECTED_TO_CHAT,
        SELECT_LOGIN_OR_REGISTER,
        LOGIN_USERNAME,
        LOGIN_PASSWORD,
        REGISTER_USERNAME,
        REGISTER_PASSWORD,
        FRIEND_REQUEST_PENDING,
        IMAGE_SENDING,
        ADD_TO_FRIENDS,
        CREATE_GROUP,
        ADD_TO_GROUP,
    }

    // Setting first state
    static AvailableStates currentState = AvailableStates.INIT;

    // Releasing mutex if there are new results available
    static public void setLoginResultAvailable(boolean result){
        loginResult = result;
        loginResultAvailable.release();
    }


    // Sending message to server
    static private void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Asynchronous friend request
    static public void friendRequest(String friendName){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
        friend = friendName;
    }



    public static void main(String[] args) {

        // Handling Discord Api


        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.XsjhmA.0dXsAYYjM2GdfIWN8egNZOhpeDY";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();


        // Setup of client-server connection
        String hostName = "localhost";
        int portNumber = 9999;
        try {
            echoSocket = new Socket(hostName, portNumber);


            out =
                    new PrintWriter(echoSocket.getOutputStream(), true);
            in =
                    new BufferedReader(
                            new InputStreamReader(echoSocket.getInputStream()));



            outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
            inObject = new ObjectInputStream( echoSocket.getInputStream() );

            // Thread made for asynchronous messages from server
            clientPrinterThread = new ClientPrinterThread(in,inObject);
            clientPrinterThread.start();

        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }



        // Discord Bot Api message handler
        api.addMessageCreateListener(event -> {
            // If bot user then dont echo back
            if(event.getMessageAuthor().isBotUser()){
                return;
            }
            
            String messageContent = event.getMessageContent();
            TextChannel channel = event.getChannel();



            // Bot state machine---------------------------------------------------------------------------------

            // Initial state. To proceed type "!chat"
            if (currentState.equals(AvailableStates.INIT) && messageContent.equalsIgnoreCase("!chat")) {
                channel.sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");
                clientPrinterThread.sendEventChannel(channel);

                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }

            // Selecting login or register option
            else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

                if(messageContent.equalsIgnoreCase("l")){
                    channel.sendMessage("You selected login. Input your Username:...");
                    currentState = AvailableStates.LOGIN_USERNAME;

                }else if(messageContent.equalsIgnoreCase("s")){
                    channel.sendMessage("You selected sign in. Input your Username:...");
                    currentState = AvailableStates.REGISTER_USERNAME;
                }


            }

            // Login path, first username --------------------------------------------------
            else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                username = messageContent;

                channel.sendMessage("Input your Password:...");
                currentState = AvailableStates.LOGIN_PASSWORD;
            }

            // Login path, password and if not correct return to selection
            else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                password = messageContent;

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password));

                // Wait for asynchronous result
                try {
                    Thread.yield();
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                if(loginResult){
                    channel.sendMessage("Login successful. Send messages: username#message_text.\n" +
                            "Send images: !image\n" +
                            "Add to friends: !friend\n" +
                            "Create group: !creategroup\n" +
                            "Add user to group: !addtogroup\n" +
                            "Change text sending to group sending: !group. Then groupname#message_text\n" +
                            "Switch to user sending: !user\n" +
                            "Quit: !q");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    channel.sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }

            }

            //-------------------------------------------------------------------------------




            // Register path, first username -------------------------------------------------
            else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                username = messageContent;
                channel.sendMessage("Input your Password:...");
                currentState = AvailableStates.REGISTER_PASSWORD;
            }


            // Register path, password and if not correct return to selection
            else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                password = messageContent;

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password));

                // Wait for asynchronous result
                try {
                    Thread.yield();
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(loginResult){
                    channel.sendMessage("Login successful. Send messages: username#message_text.\\n\" +\n" +
                            " Send images: !image\\n +\n" +
                            "Add to friends: !friend\\n +\n" +
                            "Create group: !creategroup\\n +\n" +
                            "Add user to group: !addtogroup\\n +\n" +
                            "Change text sending to group sending: !group. Then groupname#message_text\n" +
                            "Switch to user sending: !user\n" +
                            "Quit: !q");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    channel.sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }
            }
            // -----------------------------------------------------------------------------------


            // Connected to chat, now choose your options
            else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){
                // Quit
                if(messageContent.equalsIgnoreCase("!q")){
                    try {
                        echoSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;
                }
                // Image
                else if(messageContent.equalsIgnoreCase("!image")){
                    currentState = AvailableStates.IMAGE_SENDING;
                    channel.sendMessage("Input image");
                }
                // Friend
                else if(messageContent.equalsIgnoreCase("!friend")){
                    currentState  = AvailableStates.ADD_TO_FRIENDS;
                    channel.sendMessage("Input friend to add name...");
                }
                // Group creation
                else if(messageContent.equalsIgnoreCase("!creategroup")){
                    currentState = AvailableStates.CREATE_GROUP;
                    channel.sendMessage("Input group name to create...");
                }
                // Add to group
                else if(messageContent.equalsIgnoreCase("!addtogroup")){
                    currentState  = AvailableStates.ADD_TO_GROUP;
                    channel.sendMessage("Input groupname#usertoadd...");
                }
                // Group sending
                else if(messageContent.equalsIgnoreCase("!group")){
                    isGroupSending = true;
                    channel.sendMessage("Group sending!");
                }
                // User sending
                else if(messageContent.equalsIgnoreCase("!user")){
                    isGroupSending = false;
                    channel.sendMessage("User sending!");
                }

                // Send normal messages
                else if(isGroupSending) {
                    String []tmp = messageContent.split("#");

                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,tmp[0]+"#"+username+"#"+tmp[1]));

                }else{
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,messageContent.toString()));

                }


            }

            // Image option
            else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
                if(event.getMessageAttachments().size() > 0 ){
                    List<MessageAttachment> attachmentArray = event.getMessage().getAttachments();
                    if(attachmentArray.get(0)!=null) {
                        sendMessage(new ClientToServerMessage(ClientToServerMessageType.IMAGE,attachmentArray.get(0).getUrl().toString()));
                    }

                }
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            // Add to friend option
            else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,messageContent));
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            // Create group option
            else if(currentState.equals(AvailableStates.CREATE_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,messageContent));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            // Add to group option
            else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,messageContent));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            // New friend request pending
            else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
                if(messageContent.equalsIgnoreCase("Y")){
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend));
                }else if(messageContent.equalsIgnoreCase("N")){

                }else {
                    channel.sendMessage("Not recognised sign");
                }
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            // Test command
            else{
                if (messageContent.equalsIgnoreCase("!ping")) {
                    channel.sendMessage("Pong!");
                }
            }



        });


        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }


}
