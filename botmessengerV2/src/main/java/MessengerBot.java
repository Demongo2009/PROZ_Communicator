import static spark.Spark.*;


import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;

import com.clivern.racter.BotPlatform;
import com.clivern.racter.receivers.webhook.*;

import com.clivern.racter.senders.templates.*;

import com.mashape.unirest.http.exceptions.UnirestException;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Main Messenger Bot class.
 * Handles Racter API specific message receiving and sending.
 */
public class MessengerBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;
    static ObjectInputStream inObject;
    static ObjectOutputStream outObject;
    static String username;
    static String password;
    static Semaphore loginResultAvailable = new Semaphore(0);
    static boolean loginResult = false;
    static boolean isGroupSending = false;
    static String friend;
    static MessageReceivedWebhook message;
    static MessageTemplate message_tpl;
    static BotPlatform platform;

    /**
     * Available states of bot state machine.
     */
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

    /**
     * Function realising mutex when asynchronous result are available.
     * @param result type of result
      */
    static void setLoginResultAvailable(boolean result){
        loginResult = result;
        loginResultAvailable.release();
    }

    /**
     * Function sending message to server.
     * @param message message to be sent
      */
    static void sendMessageToServer(ClientToServerMessage message){
        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Function handling asynchronous friend requests.
     * @param friendName name of the friend requesting friendship
      */
    static public void friendRequest(String friendName){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
        friend = friendName;
    }

    /**
     * Function sending message to client.
     * @param text message text
      */
    static public void sendRegularMessage(String text){
        message_tpl.setRecipientId(message.getUserId());
        message_tpl.setMessageText(text);
        message_tpl.setNotificationType("REGULAR");
        try {
            platform.getBaseSender().send(message_tpl);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws IOException
    {
        // Setup connection with server
        currentState = AvailableStates.INIT;
        String hostName = "localhost";
        int portNumber = 9999;
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


            clientPrinterThread = new ClientPrinterThread(inObject);
            clientPrinterThread.start();


        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        // Verify Token Route
        get("/", (request, response) -> {
            BotPlatform platform = new BotPlatform("config.properties");
            platform.getVerifyWebhook().setHubMode(( request.queryParams("hub.mode") != null ) ? request.queryParams("hub.mode") : "");
            platform.getVerifyWebhook().setHubVerifyToken(( request.queryParams("hub.verify_token") != null ) ? request.queryParams("hub.verify_token") : "");
            platform.getVerifyWebhook().setHubChallenge(( request.queryParams("hub.challenge") != null ) ? request.queryParams("hub.challenge") : "");

            if( platform.getVerifyWebhook().challenge() ){

                response.status(200);
                return ( request.queryParams("hub.challenge") != null ) ? request.queryParams("hub.challenge") : "";
            }


            response.status(403);
            return "Verification token mismatch";
        });

        post("/", (request, response) -> {

            // Messenger Api
            String body = request.body();
            BotPlatform platformN = new BotPlatform("config.properties");
            platform = platformN;
            platform.getBaseReceiver().set(body).parse();
            HashMap<String, MessageReceivedWebhook> messages = (HashMap<String, MessageReceivedWebhook>) platform.getBaseReceiver().getMessages();
            for (MessageReceivedWebhook messageGot : messages.values()) {
                message= messageGot;

                String user_id = (message.hasUserId()) ? message.getUserId() : "";
                String page_id = (message.hasPageId()) ? message.getPageId() : "";
                String message_id = (message.hasMessageId()) ? message.getMessageId() : "";
                String message_text = (message.hasMessageText()) ? message.getMessageText() : "";
                String quick_reply_payload = (message.hasQuickReplyPayload()) ? message.getQuickReplyPayload() : "";
                Long timestamp = (message.hasTimestamp()) ? message.getTimestamp() : 0;
                HashMap<String, String> attachments = (message.hasAttachment()) ? (HashMap<String, String>) message.getAttachment() : new HashMap<String, String>();

                // Use Logger To Log Incoming Data
                Logger.info("User ID#:" + user_id);
                Logger.info("Page ID#:" + page_id);
                Logger.info("Message ID#:" + message_id);
                Logger.info("Message Text#:" + message_text);
                Logger.info("Quick Reply Payload#:" + quick_reply_payload);

                for (String attachment : attachments.values()) {
                    Logger.info("Attachment#:" + attachment);
                }


                MessageTemplate message_tplN = platform.getBaseSender().getMessageTemplate();
                message_tpl= message_tplN;



                clientPrinterThread.releaseMutex();




                // Messenger bot state machine
                if (currentState.equals(AvailableStates.INIT) && message_text.equalsIgnoreCase("!chat")) {

                    sendRegularMessage("Initiated chat!Please login[l] or sign in[s]...");

                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }
                // Login
                else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){


                    if(message_text.equalsIgnoreCase("l")){
                        sendRegularMessage("You selected login. Input your Username:...");

                        currentState = AvailableStates.LOGIN_USERNAME;

                    }else if(message_text.equalsIgnoreCase("s")){

                        sendRegularMessage("You selected sign in. Input your Username:...");

                        currentState = AvailableStates.REGISTER_USERNAME;
                    }


                }
                // Login username
                else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                    username = message_text;

                    sendRegularMessage("Input your Password:...");
                    currentState = AvailableStates.LOGIN_PASSWORD;
                }
                // Login password
                else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                    password = message_text;

                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password));

                    // asynchronous result
                    try {
                        Thread.yield();
                        loginResultAvailable.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                    if(loginResult){

                        sendRegularMessage("Login successful. Send messages: username#message_text." +
                                "Send images: !image. " +
                                "Add to friends: !friend. " +
                                "Create group: !creategroup. " +
                                "Add user to group: !addtogroup. " +
                                "Change text sending to group sending: !group. Then groupname#message_text. " +
                                "Switch to user sending: !user. " +
                                "Quit: !q");
                        currentState = AvailableStates.CONNECTED_TO_CHAT;
                    }else {

                        sendRegularMessage("Incorrect data. Try again. Please login[l] or sign in[s]...");
                        currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                    }

                }
                // Register login
                else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                    username = message_text;

                    sendRegularMessage("Input your Password:...");
                    currentState = AvailableStates.REGISTER_PASSWORD;
                }
                // Register password
                else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                    password = message_text;

                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password));

                    // asynchronous results
                    try {
                        Thread.yield();
                        loginResultAvailable.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(loginResult){

                        sendRegularMessage("Login successful. Send messages: username#message_text. " +
                                "Send images: !image. " +
                                "Add to friends: !friend. " +
                                "Create group: !creategroup. " +
                                "Add user to group: !addtogroup. " +
                                "Change text sending to group sending: !group. Then groupname#message_text. " +
                                "Switch to user sending: !user. " +
                                "Quit: !q");
                        currentState = AvailableStates.CONNECTED_TO_CHAT;
                    }else {

                        sendRegularMessage("Incorrect data. Try again. Please login[l] or sign in[s]...");
                        currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                    }
                }


                // Chat connection established, now choose options
                else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){
                    // Quit
                    if(message_text.equals("!q")){
                        try {
                            echoSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "ok";

                    }
                    // Image
                    else if(message_text.equalsIgnoreCase("!image")){
                        currentState = AvailableStates.IMAGE_SENDING;

                        sendRegularMessage("Input image");
                    }
                    // Add friend
                    else if(message_text.equalsIgnoreCase("!friend")){
                        currentState  = AvailableStates.ADD_TO_FRIENDS;

                        sendRegularMessage("Input friend to add name...");
                    }
                    // Create group
                    else if(message_text.equalsIgnoreCase("!creategroup")){
                        currentState = AvailableStates.CREATE_GROUP;

                        sendRegularMessage("Input group name to create...");
                    }
                    // Add to group
                    else if(message_text.equalsIgnoreCase("!addtogroup")){
                        currentState  = AvailableStates.ADD_TO_GROUP;

                        sendRegularMessage("Input groupname#usertoadd...");
                    }
                    // Group sending
                    else if(message_text.equalsIgnoreCase("!group")){
                        isGroupSending = true;

                        sendRegularMessage("Group sending!");
                    }
                    // User sending
                    else if(message_text.equalsIgnoreCase("!user")){
                        isGroupSending = false;

                        sendRegularMessage("User sending!");
                    }
                    // Send messsage
                    else if(isGroupSending) {
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,message_text));

                    }else{
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,message_text));

                    }





                }
                // Image sending
                else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
                    for(String a: attachments.values()){
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.IMAGE,a));
                    }
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }
                // Add to friends
                else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,message_text));
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }
                // Create group
                else if(currentState.equals(AvailableStates.CREATE_GROUP)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,message_text));

                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }
                // Add to group
                else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,message_text));

                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }
                // Asynchronous request
                else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
                    if(message_text.equalsIgnoreCase("Y")){
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend));
                    }else if(message_text.equalsIgnoreCase("N")){

                    }else {

                        sendRegularMessage("Not recognised sign");
                    }
                }

                // for testing
                else if( message_text.equals("image") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("image", "http://techslides.com/demos/samples/sample.jpg", false);
                    message_tpl.setNotificationType("SILENT_PUSH");
                    platform.getBaseSender().send(message_tpl);

                }else if( message_text.equals("file") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("file", "http://techslides.com/demos/samples/sample.pdf", false);
                    message_tpl.setNotificationType("NO_PUSH");
                    platform.getBaseSender().send(message_tpl);

                }else if( message_text.equals("video") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("video", "http://techslides.com/demos/samples/sample.mp4", false);
                    platform.getBaseSender().send(message_tpl);

                }else if( message_text.equals("audio") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("audio", "http://techslides.com/demos/samples/sample.mp3", false);
                    platform.getBaseSender().send(message_tpl);

                }

                return "ok";
            }

            // ..
            // Other Receive Webhooks Goes Here
            // ..

            return "No Messages";
        });
    }
}