import static spark.Spark.*;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Server.CommunicatorType;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.receivers.webhook.*;

import com.clivern.racter.senders.templates.*;
import org.pmw.tinylog.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;


public class MessengerBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;
    static ObjectInputStream inObject;
    static ObjectOutputStream outObject;

    enum AvailableStates{
        INIT,
        CONNECTED,
    }
    static AvailableStates currentState = AvailableStates.INIT;

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

            if( true ){
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

                if( message_text.equals("hello") && currentState == AvailableStates.INIT ){

//                    System.out.println("ty");
                    outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.MESSENGER));

                    currentState = AvailableStates.CONNECTED;

                }
                else if( currentState == AvailableStates.CONNECTED ){

                    for(String a: attachments.values()){
                        outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.IMAGE,a,CommunicatorType.MESSENGER));
                    }
                    if(!message_text.equals("")){
                        outObject.writeObject(new ClientToServerMessage(ClientToServerMessageType.TEXT,"messenger#"+message_text,CommunicatorType.MESSENGER));
                    }


                    if (message_text == "Quit"){
                        currentState = AvailableStates.INIT;
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