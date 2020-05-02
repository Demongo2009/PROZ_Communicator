package com.clivern.dunk;

import static spark.Spark.*;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.receivers.webhook.*;

import com.clivern.racter.senders.*;
import com.clivern.racter.senders.templates.*;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {

    static void send(String args){



//            BufferedReader in =
//                    new BufferedReader(
//                            new InputStreamReader(echoSocket.getInputStream()));
//            BufferedReader stdIn =
//                    new BufferedReader(
//                            new InputStreamReader(System.in));

            // thread for printing client messages
//            ClientPrinterThread clientPrinterThread = new ClientPrinterThread(in);
//            clientPrinterThread.start();



            // sending message to server
            if (args != null) {
                out.println(args);
//                System.out.println("echo: " + in.readLine());
            }




    }

    static enum AvailableStates{
        WAITING_FOR_HELLO,
        WAITING_FOR_USERNAME,
        WAITING_FOR_GROUP,
        CONNECTED_TO_GROUP,

    }

    static AvailableStates currentState = AvailableStates.WAITING_FOR_HELLO;
    static PrintWriter out;
    static Socket echoSocket;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;

    public static void main(String[] args) throws IOException
    {


        // Verify Token Route
        get("/", (request, response) -> {
            BotPlatform platform = new BotPlatform("src/main/java/resources/config.properties");
            platform.getVerifyWebhook().setHubMode(( request.queryParams("hub.mode") != null ) ? request.queryParams("hub.mode") : "");
            platform.getVerifyWebhook().setHubVerifyToken(( request.queryParams("hub.verify_token") != null ) ? request.queryParams("hub.verify_token") : "");
            platform.getVerifyWebhook().setHubChallenge(( request.queryParams("hub.challenge") != null ) ? request.queryParams("hub.challenge") : "");

            if( platform.getVerifyWebhook().challenge() ){
                platform.finish();
                response.status(200);
                return ( request.queryParams("hub.challenge") != null ) ? request.queryParams("hub.challenge") : "";
            }

            platform.finish();
            response.status(403);
            return "Verification token mismatch";
        });

        post("/", (request, response) -> {
            String body = request.body();
            BotPlatform platform = new BotPlatform("src/main/java/resources/config.properties");
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

                platform.getLogger().info("User ID#:" + user_id);
                platform.getLogger().info("Page ID#:" + page_id);
                platform.getLogger().info("Message ID#:" + message_id);
                platform.getLogger().info("Message Text#:" + message_text);
                platform.getLogger().info("Quick Reply Payload#:" + quick_reply_payload);

                for (String attachment : attachments.values()) {
                    platform.getLogger().info("Attachment#:" + attachment);
                }

                String text = message.getMessageText();
                MessageTemplate message_tpl = platform.getBaseSender().getMessageTemplate();
                ButtonTemplate button_message_tpl = platform.getBaseSender().getButtonTemplate();
                ListTemplate list_message_tpl = platform.getBaseSender().getListTemplate();
                GenericTemplate generic_message_tpl = platform.getBaseSender().getGenericTemplate();
                ReceiptTemplate receipt_message_tpl = platform.getBaseSender().getReceiptTemplate();

                clientPrinterThread.initializeMessage(text,message.getUserId());
                clientPrinterThread.initializePlatform();
                clientPrinterThread.releaseMutex();






                if( text.equals("hello") && currentState == AvailableStates.WAITING_FOR_HELLO ){
                    String hostName = "localhost";
                    int portNumber = 4444;
                    try {
                        echoSocket = new Socket(hostName, portNumber);
                        // shutdown hook added for closing the connection if client exits
                        Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));
                        out =
                                new PrintWriter(echoSocket.getOutputStream(), true);
                        in =
                                new BufferedReader(
                                        new InputStreamReader(echoSocket.getInputStream()));


                        clientPrinterThread = new ClientPrinterThread(in);
                        clientPrinterThread.start();

//            send("hello");

                    }catch (UnknownHostException e) {
                        e.printStackTrace();
                    }catch (IOException e) {
                        e.printStackTrace();
                    }

                    send(text);
//                    message_tpl.setRecipientId(message.getUserId());
//                    message_tpl.setMessageText("Enter your username...");
//                    message_tpl.setNotificationType("REGULAR");
//                    platform.getBaseSender().send(message_tpl);
                    currentState = AvailableStates.WAITING_FOR_USERNAME;

                }
                else if( currentState == AvailableStates.WAITING_FOR_USERNAME ){

                    send(text);
//                    message_tpl.setRecipientId(message.getUserId());
//                    message_tpl.setMessageText("Your username is: "+ text +". Please enter your group id...");
//                    message_tpl.setNotificationType("REGULAR");
//                    platform.getBaseSender().send(message_tpl);
//                    currentState = AvailableStates.WAITING_FOR_GROUP;
                    if (text == "Quit"){
                        currentState = AvailableStates.WAITING_FOR_HELLO;
                    }

                }
//                else if( currentState == AvailableStates.WAITING_FOR_GROUP ){
//
//                    send(text);
//                    message_tpl.setRecipientId(message.getUserId());
//                    message_tpl.setMessageText("You try to connect to: "+ text+" ...");
//                    message_tpl.setNotificationType("REGULAR");
//                    platform.getBaseSender().send(message_tpl);
//
//                }
//                else if( currentState == AvailableStates.CONNECTED_TO_GROUP ){
//
//                    send(text);
//                    message_tpl.setRecipientId(message.getUserId());
//                    message_tpl.setMessageText("Succesfully connected. ");
//                    message_tpl.setNotificationType("REGULAR");
//                    platform.getBaseSender().send(message_tpl);
//
//                }




                else if( text.equals("image") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("image", "http://techslides.com/demos/samples/sample.jpg", false);
                    message_tpl.setNotificationType("SILENT_PUSH");
                    platform.getBaseSender().send(message_tpl);

                }else if( text.equals("file") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("file", "http://techslides.com/demos/samples/sample.pdf", false);
                    message_tpl.setNotificationType("NO_PUSH");
                    platform.getBaseSender().send(message_tpl);

                }else if( text.equals("video") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("video", "http://techslides.com/demos/samples/sample.mp4", false);
                    platform.getBaseSender().send(message_tpl);

                }else if( text.equals("audio") ){

                    message_tpl.setRecipientId(message.getUserId());
                    message_tpl.setAttachment("audio", "http://techslides.com/demos/samples/sample.mp3", false);
                    platform.getBaseSender().send(message_tpl);

                }
                return "ok";
            }
            return "bla";
        });
    }
}