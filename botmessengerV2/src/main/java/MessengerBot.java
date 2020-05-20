import static spark.Spark.*;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Server.CommunicatorType;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.receivers.webhook.*;

import com.clivern.racter.senders.templates.*;
import com.mashape.unirest.http.exceptions.UnirestException;
import javafx.application.Platform;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;


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

    static void setLoginResultAvailable(boolean result){
        loginResultAvailable.release();
        loginResult = result;
    }

    static void sendMessageToServer(ClientToServerMessage message){
        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static public void friendRequest(String friendName){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
        friend = friendName;
    }



    public static void main(String[] args) throws IOException
    {
        currentState = AvailableStates.INIT;
        String hostName = "localhost";
        int portNumber = 4444;
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
//            outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.TEXT,"dsa#sda",CommunicatorType.MESSENGER));

            String body = request.body();
            BotPlatform platform = new BotPlatform("config.properties");
            platform.getBaseReceiver().set(body).parse();
            HashMap<String, MessageReceivedWebhook> messages = (HashMap<String, MessageReceivedWebhook>) platform.getBaseReceiver().getMessages();
            for (MessageReceivedWebhook message : messages.values()) {

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


                MessageTemplate message_tpl = platform.getBaseSender().getMessageTemplate();
                ButtonTemplate button_message_tpl = platform.getBaseSender().getButtonTemplate();
                ListTemplate list_message_tpl = platform.getBaseSender().getListTemplate();
                GenericTemplate generic_message_tpl = platform.getBaseSender().getGenericTemplate();
                ReceiptTemplate receipt_message_tpl = platform.getBaseSender().getReceiptTemplate();

                clientPrinterThread.initializeMessage(message_text,message.getUserId());
                clientPrinterThread.initializePlatform();
                clientPrinterThread.releaseMutex();

//            outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.TEXT,"dsa#sda",CommunicatorType.MESSENGER));

//                if( message_text.equals("hello") && currentState == AvailableStates.INIT ){
//
////                    System.out.println("ty");
//                    outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.MESSENGER));
//
//                    currentState = AvailableStates.CONNECTED;
//
//                }
//                else if( currentState == AvailableStates.CONNECTED ){
//
//                    for(String a: attachments.values()){
//                        outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.IMAGE,a,CommunicatorType.MESSENGER));
//                    }
//                    if(!message_text.equals("")){
//                        outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.TEXT,"messenger#"+message_text,CommunicatorType.MESSENGER));
//                    }
//
//
//                    if (message_text == "Quit"){
//                        currentState = AvailableStates.INIT;
//                    }
//
//                }

                SenderWrapper senderWrapper = new SenderWrapper(user_id);

//                System.out.println("tu "+message_text);
                if (currentState.equals(AvailableStates.INIT) && message_text.equalsIgnoreCase("!chat")) {
//                    event.getChannel().sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");
//                    System.out.println("tam "+message_text);

                    senderWrapper.sendToMessenger("Initiated chat!\nPlease login[l] or sign in[s]...");

//                    clientPrinterThread.sendRegularMessage("Initiated chat!\nPlease login[l] or sign in[s]...");



                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }

                else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){
//                    System.out.println("tamtam "+message_text);

                    if(message_text.equalsIgnoreCase("l")){
//                        event.getChannel().sendMessage("You selected login. Input your Username:...");
                        senderWrapper.sendToMessenger("You selected login. Input your Username:...");

                        currentState = AvailableStates.LOGIN_USERNAME;

                    }else if(message_text.equalsIgnoreCase("s")){
//                        event.getChannel().sendMessage("You selected sign in. Input your Username:...");
                        senderWrapper.sendToMessenger("You selected sign in. Input your Username:...");

                        currentState = AvailableStates.REGISTER_USERNAME;
                    }


                }

                else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                    username = message_text;
//                    System.out.println(username);
                    senderWrapper.sendToMessenger("Input your Password:...");
                    currentState = AvailableStates.LOGIN_PASSWORD;
                }

                else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                    password = message_text;

                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password,CommunicatorType.MESSENGER));
//                    System.out.println("Waiting for login result");
                    try {
                        Thread.yield();
                        loginResultAvailable.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
//                    System.out.println("Result got!");

                    if(loginResult){
                        senderWrapper.sendToMessenger("Login successful. Send messages: username#message_text.\n" +
                                "Send images: !image\n" +
                                "Add to friends: !friend\n" +
                                "Create group: !creategroup\n" +
                                "Add user to group: !addtogroup\n" +
                                "Change text sending to group sending: !group. Then groupname#message_text\n" +
                                "Switch to user sending: !user\n" +
                                "Quit: !q");
                        currentState = AvailableStates.CONNECTED_TO_CHAT;
                    }else {
                        senderWrapper.sendToMessenger("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                        currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                    }

                }

                else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                    username = message_text;
                    senderWrapper.sendToMessenger("Input your Password:...");
                    currentState = AvailableStates.REGISTER_PASSWORD;
                }

                else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                    password = message_text;

                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password,CommunicatorType.MESSENGER));

                    try {
                        Thread.yield();
                        loginResultAvailable.acquire();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if(loginResult){
                        senderWrapper.sendToMessenger("Login successful. Send messages: username#message_text.\\n\" +\n" +
                                "                            \"Send images: !image\\n\" +\n" +
                                "                            \"Add to friends: !friend\\n\" +\n" +
                                "                            \"Create group: !creategroup\\n\" +\n" +
                                "                            \"Add user to group: !addtogroup\\n\" +\n" +
                                "                            \"Change text sending to group sending: !group. Then groupname#message_text\n" +
                                "Switch to user sending: !user\n" +
                                "Quit: !q");
                        currentState = AvailableStates.CONNECTED_TO_CHAT;
                    }else {
                        senderWrapper.sendToMessenger("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                        currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                    }
                }

                else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
                    for(String a: attachments.values()){
                       sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.IMAGE,a,CommunicatorType.MESSENGER));
                    }
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }

                else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,message_text,CommunicatorType.MESSENGER));
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }

                else if(currentState.equals(AvailableStates.CREATE_GROUP)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,message_text,CommunicatorType.MESSENGER));

                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }

                else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
                    sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,message_text,CommunicatorType.MESSENGER));

                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }

                else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){

                    if(message_text.equals("!q")){
                        try {
                            echoSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "ok";

                    }else if(message_text.equalsIgnoreCase("!image")){
                        currentState = AvailableStates.IMAGE_SENDING;
                        senderWrapper.sendToMessenger("Input image");
                    }else if(message_text.equalsIgnoreCase("!friend")){
                        currentState  = AvailableStates.ADD_TO_FRIENDS;
                        senderWrapper.sendToMessenger("Input friend to add name...");
                    }else if(message_text.equalsIgnoreCase("!creategroup")){
                        currentState = AvailableStates.CREATE_GROUP;
                        senderWrapper.sendToMessenger("Input group name to create...");
                    }else if(message_text.equalsIgnoreCase("!addtogroup")){
                        currentState  = AvailableStates.ADD_TO_GROUP;
                        senderWrapper.sendToMessenger("Input groupname#usertoadd...");
                    }else if(message_text.equalsIgnoreCase("!group")){
                        isGroupSending = true;
                        senderWrapper.sendToMessenger("Group sending!");
                    }else if(message_text.equalsIgnoreCase("!user")){
                        isGroupSending = false;
                        senderWrapper.sendToMessenger("User sending!");
                    }
                    else if(isGroupSending) {
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,message_text,CommunicatorType.MESSENGER));

                    }else{
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,message_text,CommunicatorType.MESSENGER));

                    }





                }

                else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
                    if(message_text.equalsIgnoreCase("Y")){
                        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend,CommunicatorType.MESSENGER));
                    }else if(message_text.equalsIgnoreCase("N")){

                    }else {
                        senderWrapper.sendToMessenger("Not recognised sign");
                    }
                }


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