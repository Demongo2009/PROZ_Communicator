import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import Server.CommunicatorType;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Multicom extends TelegramLongPollingBot {
    private PrintWriter out;
    private BufferedReader in;
    private ObjectOutputStream outObject;
    private ObjectInputStream inObject;

    public Multicom(PrintWriter out, BufferedReader in, ObjectOutputStream outObject, ObjectInputStream inObject){
        this.out = out;
        this.in = in;
        this.outObject = outObject;
        this.inObject = inObject;
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

    private void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Message message = update.getMessage();

                handleIncomingMessage(message);

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


        String text="";
        if(message.hasText()){

             text = message.getText();
        }
        System.out.println(text);

        if(state.equals(AvailableStates.INIT) && text.equals("!chat")){
            echoMessage.setText("Connected to chat!\n");
            execute(echoMessage);
            state = AvailableStates.CONNECTED;


            sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.TELEGRAM));

            Thread thread = new Thread(){
                @Override
                public void run() {

                    try {
                        String inputFromServer;
                        ServerToClientMessage messageFromServer = null;
                        while ((messageFromServer = receiveMessage()) != null) {
                            inputFromServer = messageFromServer.getText();
                            if (inputFromServer.equals("") || inputFromServer.equals("\n")) {
                                continue;
                            }

                            if(messageFromServer.getType().equals(ServerToClientMessageType.IMAGE)){
                                SendPhoto photoMessage = new SendPhoto().setPhoto(inputFromServer);
                                execute(photoMessage);
                            }else{

                                echoMessage.setText(inputFromServer);
                                execute(echoMessage);
                            }


                        }

                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();
        }else if(state.equals(AvailableStates.CONNECTED)){
            if(message.hasPhoto()){
                GetFile getFileRequest = new GetFile();

                getFileRequest.setFileId(message.getPhoto().get(0).getFileId());
                File file = execute(getFileRequest);
                String fileURL = file.getFileUrl("827656409:AAEgFLohXzB9sdkWUIaKz4IaYnAF16dZOrU");
                System.out.println(fileURL);
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.IMAGE,fileURL,CommunicatorType.TELEGRAM));

            }else{
                sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT,"telegram#"+text,CommunicatorType.TELEGRAM));

            }
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
