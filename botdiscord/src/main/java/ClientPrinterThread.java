
import org.javacord.api.entity.channel.TextChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.Semaphore;

public class ClientPrinterThread extends Thread {
    BufferedReader in;

    ClientPrinterThread(BufferedReader in){
        this.in=in;

        mutex = new Semaphore(0);

    }

    public void initializeMessage( String text, String userId){
        this.text = text;
        this.userId = userId;
    }
    String text;
    String userId;
    TextChannel textChannel;

    public void sendEventChannel(TextChannel textChannel){
        this.textChannel = textChannel;
    }

//    public void initializePlatform(){
//        platform = null;
//        try {
//            platform = new BotPlatform("src/main/java/resources/config.properties");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        message_tpl = platform.getBaseSender().getMessageTemplate();
//        button_message_tpl = platform.getBaseSender().getButtonTemplate();
//        list_message_tpl = platform.getBaseSender().getListTemplate();
//        generic_message_tpl = platform.getBaseSender().getGenericTemplate();
//        receipt_message_tpl = platform.getBaseSender().getReceiptTemplate();
//    }
//    BotPlatform platform;
//    MessageTemplate message_tpl;
//    ButtonTemplate button_message_tpl;
//    ListTemplate list_message_tpl;
//    GenericTemplate generic_message_tpl;
//    ReceiptTemplate receipt_message_tpl;

    static Semaphore mutex;

    public void releaseMutex(){
        mutex.release();
    }

    public void run(){
        try{
            String inputFromServer;
            mutex.acquire();
            while((inputFromServer = in.readLine()) != null){
                if(inputFromServer == "" || inputFromServer == "\n"){
                    continue;
                }
//                System.out.println("Server.Server: "+ inputFromServer);
//                message_tpl.setRecipientId(userId);
//                message_tpl.setMessageText("Server: "+ inputFromServer);
//                message_tpl.setNotificationType("REGULAR");
//                try {
//                    platform.getBaseSender().send(message_tpl);
//                } catch (UnirestException e) {
//                    e.printStackTrace();
//                }
                textChannel.sendMessage(inputFromServer);
            }

        }catch (IOException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
