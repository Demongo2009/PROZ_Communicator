import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Server.CommunicatorType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
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
    public static Semaphore loginResultAvailable = new Semaphore(0);
    public static boolean loginResult;
    public static String friend;
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



    static private void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void friendRequest(){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
    }



    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.XsKkzg.hxQf5HAH9fK3r40-nq9GFTkA_7s";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();




        Thread thread = new Thread(){
            public void run(){
                String hostName = "localhost";
                int portNumber = 4444;
                try {
                    echoSocket = new Socket(hostName, portNumber);
                    Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));

                    // shutdown hook added for closing the connection if client exits
                    out =
                            new PrintWriter(echoSocket.getOutputStream(), true);
                    in =
                            new BufferedReader(
                                    new InputStreamReader(echoSocket.getInputStream()));



                    outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
                    inObject = new ObjectInputStream( echoSocket.getInputStream() );

                    clientPrinterThread = new ClientPrinterThread(in,inObject);
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
            if (currentState.equals(AvailableStates.INIT) && event.getMessageContent().equalsIgnoreCase("!chat")) {
                event.getChannel().sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");
                clientPrinterThread.sendEventChannel(event.getChannel());

                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }

            else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

                if(event.getMessageContent().equalsIgnoreCase("l")){
                    event.getChannel().sendMessage("You selected login. Input your Username:...");
                    currentState = AvailableStates.LOGIN_USERNAME;

                }else if(event.getMessageContent().equalsIgnoreCase("s")){
                    event.getChannel().sendMessage("You selected sign in. Input your Username:...");
                    currentState = AvailableStates.REGISTER_USERNAME;
                }


//                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.DISCORD));
//
//                clientPrinterThread.sendEventChannel(event.getChannel());
//                clientPrinterThread.releaseMutex();
//
//                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                username = event.getMessageContent();
                System.out.println(username);
                event.getChannel().sendMessage("Input your Password:...");
                currentState = AvailableStates.LOGIN_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                password = event.getMessageContent();

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password,CommunicatorType.DISCORD));
                System.out.println("Waiting for login result");
                try {
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Result got!");

                if(loginResult){
                    event.getChannel().sendMessage("Login successful. Send messages: username#message_text.\n" +
                            "Send images: !image\n" +
                            "Add to friends: !friend\n" +
                            "Create group: !creategroup\n" +
                            "Add user to group: !addtogroup\n" +
                            "Change text sending to group sending: !group. Then groupname#message_text\n" +
                            "Quit: !q");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    event.getChannel().sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }

            }

            else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                username = event.getMessageContent();
                event.getChannel().sendMessage("Input your Password:...");
                currentState = AvailableStates.REGISTER_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                password = event.getMessageContent();

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password,CommunicatorType.DISCORD));

                try {
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(loginResult){
                    event.getChannel().sendMessage("Login successful. Send messages: username#message_text.\\n\" +\n" +
                            "                            \"Send images: !image\\n\" +\n" +
                            "                            \"Add to friends: !friend\\n\" +\n" +
                            "                            \"Create group: !creategroup\\n\" +\n" +
                            "                            \"Add user to group: !addtogroup\\n\" +\n" +
                            "                            \"Change text sending to group sending: !group. Then groupname#message_text\n" +
                            "Quit: !q");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    event.getChannel().sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
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
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,event.getMessageContent(),CommunicatorType.DISCORD));
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.CREATE_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,event.getMessageContent(),CommunicatorType.DISCORD));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,event.getMessageContent(),CommunicatorType.DISCORD));

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){

                if(event.getMessageContent().equalsIgnoreCase("!q")){
                    try {
                        echoSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return;

                }else if(event.getMessageContent().equalsIgnoreCase("!image")){
                    currentState = AvailableStates.IMAGE_SENDING;
                    event.getChannel().sendMessage("Input image");
                }else if(event.getMessageContent().equalsIgnoreCase("!friend")){
                    currentState  = AvailableStates.ADD_TO_FRIENDS;
                    event.getChannel().sendMessage("Input friend to add name...");
                }else if(event.getMessageContent().equalsIgnoreCase("!creategroup")){
                    currentState = AvailableStates.CREATE_GROUP;
                    event.getChannel().sendMessage("Input group name to create...");
                }else if(event.getMessageContent().equalsIgnoreCase("!addtogroup")){
                    currentState  = AvailableStates.ADD_TO_GROUP;
                    event.getChannel().sendMessage("Input groupname#usertoadd...");
                }else if(event.getMessageContent().equalsIgnoreCase("!group")){
                    isGroupSending = true;
                }else if(isGroupSending) {
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,event.getMessageContent(),CommunicatorType.DISCORD));

                }else{
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,event.getMessageContent().toString(),CommunicatorType.DISCORD));

                }





            }

            else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
                if(event.getMessageContent().equalsIgnoreCase("Y")){
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend,CommunicatorType.DISCORD));
                }else if(event.getMessageContent().equalsIgnoreCase("N")){

                }else {
                    event.getChannel().sendMessage("Not recognised sign");
                }
            }
            else{
                if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                    event.getChannel().sendMessage("Pong!");
                }
                if (event.getMessageContent().equalsIgnoreCase("!elka")) {
                    event.getChannel().sendMessage(" * * * * * elke!");
                }
            }



        });


        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }


}
