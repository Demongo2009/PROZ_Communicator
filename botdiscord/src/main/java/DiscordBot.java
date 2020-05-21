import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Server.CommunicatorType;
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

    static AvailableStates currentState = AvailableStates.INIT;

    static public void setLoginResultAvailable(boolean result){
        loginResult = result;
        loginResultAvailable.release();
    }



    static private void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void friendRequest(String friendName){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
        friend = friendName;
    }



    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.XsZWlQ.xqlMNifO8o0-8BY2pONil4b0c6s";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();




        Thread thread = new Thread(){
            public void run(){
                String hostName = "localhost";
                int portNumber = 45000;
                try {
                    echoSocket = new Socket(hostName, portNumber);


                    // shutdown hook added for closing the connection if client exits
                    out =
                            new PrintWriter(echoSocket.getOutputStream(), true);
                    in =
                            new BufferedReader(
                                    new InputStreamReader(echoSocket.getInputStream()));



                    outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
                    inObject = new ObjectInputStream( echoSocket.getInputStream() );

                    clientPrinterThread = new ClientPrinterThread(in,inObject);
                    clientPrinterThread.setPriority(10);
                    clientPrinterThread.start();

                }catch (UnknownHostException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();




        api.addMessageCreateListener(event -> {
            if(event.getMessageAuthor().isBotUser()){
                return;
            }
            
            String messageContent = event.getMessageContent();
            TextChannel channel = event.getChannel();
            if (currentState.equals(AvailableStates.INIT) && messageContent.equalsIgnoreCase("!chat")) {
                channel.sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");
                clientPrinterThread.sendEventChannel(channel);

                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }

            else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

                if(messageContent.equalsIgnoreCase("l")){
                    channel.sendMessage("You selected login. Input your Username:...");
                    currentState = AvailableStates.LOGIN_USERNAME;

                }else if(messageContent.equalsIgnoreCase("s")){
                    channel.sendMessage("You selected sign in. Input your Username:...");
                    currentState = AvailableStates.REGISTER_USERNAME;
                }


//                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.DISCORD));
//
//                clientPrinterThread.sendEventChannel(channel);
//                clientPrinterThread.releaseMutex();
//
//                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                username = messageContent;
                System.out.println(username);
                channel.sendMessage("Input your Password:...");
                currentState = AvailableStates.LOGIN_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                password = messageContent;

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password,CommunicatorType.DISCORD));
//                System.out.println("Waiting for login result");
                try {
//                    Thread.sleep(10000);
                    Thread.yield();
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                System.out.println("Result got!");

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

            else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                username = messageContent;
                channel.sendMessage("Input your Password:...");
                currentState = AvailableStates.REGISTER_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                password = messageContent;

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password,CommunicatorType.DISCORD));

                try {
                    Thread.yield();
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(loginResult){
                    channel.sendMessage("Login successful. Send messages: username#message_text.\\n\" +\n" +
                            "                            \"Send images: !image\\n\" +\n" +
                            "                            \"Add to friends: !friend\\n\" +\n" +
                            "                            \"Create group: !creategroup\\n\" +\n" +
                            "                            \"Add user to group: !addtogroup\\n\" +\n" +
                            "                            \"Change text sending to group sending: !group. Then groupname#message_text\n" +
                            "Switch to user sending: !user\n" +
                            "Quit: !q");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    channel.sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }
            }

            else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
                if(event.getMessageAttachments().size() > 0 ){
                    List<MessageAttachment> attachmentArray = event.getMessage().getAttachments();
                    if(attachmentArray.get(0)!=null) {
                        sendMessage(new ClientToServerMessage(ClientToServerMessageType.IMAGE,attachmentArray.get(0).getUrl().toString(), CommunicatorType.DISCORD));
                    }

                }
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,messageContent,CommunicatorType.DISCORD));
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.CREATE_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,messageContent,CommunicatorType.DISCORD));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,messageContent,CommunicatorType.DISCORD));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){

                if(messageContent.equalsIgnoreCase("!q")){
                    try {
                        echoSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;

                }else if(messageContent.equalsIgnoreCase("!image")){
                    currentState = AvailableStates.IMAGE_SENDING;
                    channel.sendMessage("Input image");
                }else if(messageContent.equalsIgnoreCase("!friend")){
                    currentState  = AvailableStates.ADD_TO_FRIENDS;
                    channel.sendMessage("Input friend to add name...");
                }else if(messageContent.equalsIgnoreCase("!creategroup")){
                    currentState = AvailableStates.CREATE_GROUP;
                    channel.sendMessage("Input group name to create...");
                }else if(messageContent.equalsIgnoreCase("!addtogroup")){
                    currentState  = AvailableStates.ADD_TO_GROUP;
                    channel.sendMessage("Input groupname#usertoadd...");
                }else if(messageContent.equalsIgnoreCase("!group")){
                    isGroupSending = true;
                    channel.sendMessage("Group sending!");
                }else if(messageContent.equalsIgnoreCase("!user")){
                    isGroupSending = false;
                    channel.sendMessage("User sending!");
                }

                else if(isGroupSending) {
                    String []tmp = messageContent.split("#");
                    System.out.println(messageContent);

                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,tmp[0]+"#"+username+"#"+tmp[1],CommunicatorType.DISCORD));

                }else{
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,messageContent.toString(),CommunicatorType.DISCORD));

                }





            }

            else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
                if(messageContent.equalsIgnoreCase("Y")){
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend,CommunicatorType.DISCORD));
                }else if(messageContent.equalsIgnoreCase("N")){

                }else {
                    channel.sendMessage("Not recognised sign");
                }
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }
            else{
                if (messageContent.equalsIgnoreCase("!ping")) {
                    channel.sendMessage("Pong!");
                }
                if (messageContent.equalsIgnoreCase("!elka")) {
                    channel.sendMessage(" * * * * * elke!");
                }
            }



        });


        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }


}
