import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.senders.templates.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
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
            platform = new BotPlatform("src/main/java/resources/config.properties");
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
                if(message.getType().equals(ServerToClientMessageType.IMAGE)){
                    message_tpl.setAttachment("image",inputFromServer,true);
                }else{
                    message_tpl.setMessageText("Server: "+ inputFromServer);

                }
                message_tpl.setNotificationType("REGULAR");
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
