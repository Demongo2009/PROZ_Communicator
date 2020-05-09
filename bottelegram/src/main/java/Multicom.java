import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Multicom extends TelegramLongPollingBot {
    private PrintWriter out;
    private BufferedReader in;

    public Multicom( PrintWriter out, BufferedReader in){
        this.out = out;
        this.in = in;
    }

//    public BotApiMethod onWebhookUpdateReceived(Update update) {
//        if (update.hasMessage() && update.getMessage().hasText()) {
//            SendMessage sendMessage = new SendMessage();
//            sendMessage.setChatId(update.getMessage().getChatId().toString());
//            sendMessage.setText("Well, all information looks like noise until you break the code.");
//            return sendMessage;
//        }
//        return null;
//    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();
                if (message.hasText()) {
                    handleIncomingMessage(message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    enum AvailableStates{
        INIT,
        CONNECTED,
    }
    private AvailableStates state = AvailableStates.INIT;

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage echoMessage = new SendMessage();
        echoMessage.setChatId(message.getChatId());




        String text = message.getText();
        System.out.println(text);

        if(state.equals(AvailableStates.INIT) && text.equals("!chat")){
            echoMessage.setText("Connected to chat!\n");
            execute(echoMessage);
            state = AvailableStates.CONNECTED;

            System.out.println(text);

            out.println("init");

            Thread thread = new Thread(){
                @Override
                public void run() {

                    try {
                        String inputFromServer;
                        while ((inputFromServer = in.readLine()) != null) {
                            if (inputFromServer == "" || inputFromServer == "\n") {
                                continue;
                            }
                            echoMessage.setText(inputFromServer);
                            execute(echoMessage);

                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }else if(state.equals(AvailableStates.CONNECTED)){
            out.println(text);
        }
    }

//    @Override
//    public void onUpdatesReceived(List<Update> updates) {
//
//    }

    public String getBotUsername() {
        return "MultiComEitiBot";
    }

    public String getBotToken() {
        return "827656409:AAEgFLohXzB9sdkWUIaKz4IaYnAF16dZOrU";
    }

//    public String getBotPath() {
//        return "updates";
//    }
}
