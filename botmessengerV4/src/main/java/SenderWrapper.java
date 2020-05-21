import com.clivern.racter.BotPlatform;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.senders.templates.MessageTemplate;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.IOException;

public class SenderWrapper {

    MessageTemplate message_tpl;
    String user_id;
    BotPlatform platform;
    SenderWrapper(String user_id){
        this.user_id = user_id;

        platform = null;
        try {
            platform = new BotPlatform("config.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.message_tpl = platform.getBaseSender().getMessageTemplate();
    }


    public void sendToMessenger(String text){
        message_tpl.setRecipientId(user_id);
        message_tpl.setMessageText(text);
        message_tpl.setNotificationType("REGULAR");
        try {
            platform.getBaseSender().send(message_tpl);
        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }
}