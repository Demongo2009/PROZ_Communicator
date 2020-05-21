import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.senders.templates.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.concurrent.Semaphore;

//import com.clivern.racter.BotPlatform;

public class ClientPrinterThread extends Thread {

    ObjectInputStream inObject;

    ClientPrinterThread(ObjectInputStream inObject){

        this.inObject = inObject;

        mutex = new Semaphore(0);

    }

    public void initializeMessage( String text, String userId){
        this.text = text;
        this.userId = userId;
    }
    String text;
    String userId;

    public void initializePlatform(){
        platform = null;
        try {
            platform = new BotPlatform("config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        message_tpl = platform.getBaseSender().getMessageTemplate();
        button_message_tpl = platform.getBaseSender().getButtonTemplate();
        list_message_tpl = platform.getBaseSender().getListTemplate();
        generic_message_tpl = platform.getBaseSender().getGenericTemplate();
        receipt_message_tpl = platform.getBaseSender().getReceiptTemplate();
    }
    BotPlatform platform;
    MessageTemplate message_tpl;
    ButtonTemplate button_message_tpl;
    ListTemplate list_message_tpl;
    GenericTemplate generic_message_tpl;
    ReceiptTemplate receipt_message_tpl;

    static Semaphore mutex;

    public void releaseMutex(){
        mutex.release();
    }

    private ServerToClientMessage receiveMessage(){
        ServerToClientMessage message = null;

        try {
            message = (ServerToClientMessage)inObject.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return message;
    }

    public void sendRegularMessage(String text){
        message_tpl.setRecipientId(userId);
        message_tpl.setNotificationType("REGULAR");
        message_tpl.setMessageText(text);
        try {
            platform.getBaseSender().send(message_tpl);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

    public void run(){
        try{
            String inputFromServer;
            mutex.acquire();
            ServerToClientMessage message = null;
            while((message = receiveMessage()) != null){
                inputFromServer = message.getText();
                if(inputFromServer == "" || inputFromServer == "\n"){
                    continue;
                }
//                System.out.println("Server.Server: "+ inputFromServer);

                message_tpl.setRecipientId(userId);
                message_tpl.setNotificationType("REGULAR");

                ServerToClientMessageType messageType= message.getType();
                if(messageType.equals(ServerToClientMessageType.IMAGE)){
                    message_tpl.setAttachment("image",inputFromServer,true);

                }else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {
                    System.out.println("tak");
                    MessengerBot.setLoginResultAvailable(true);

                }else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {
                    System.out.println("nie");
                    MessengerBot.setLoginResultAvailable(false);


                }else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {
                    System.out.println("friend attempt");
                    message_tpl.setMessageText("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                    MessengerBot.friendRequest(inputFromServer);

                }else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){
                    message_tpl.setMessageText("\""+inputFromServer + "\" accepted your friend request");
                }
                else if(messageType.equals(ServerToClientMessageType.USER_ADDED_YOU_TO_GROUP)){
                    message_tpl.setMessageText("You've been added to group: \""+inputFromServer + "\"");
                }
                else {
                    message_tpl.setMessageText(inputFromServer);
                }

                try {
                    platform.getBaseSender().send(message_tpl);
                } catch (UnirestException e) {
                    e.printStackTrace();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}