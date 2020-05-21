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
    String username;
    String password;
    Semaphore loginResultAvailable = new Semaphore(0);
    boolean loginResult = false;
    boolean isGroupSending = false;
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

    void setLoginResultAvailable(boolean result){
        loginResultAvailable.release();
        loginResult = result;
    }

    void sendMessageToServer(ClientToServerMessage message){
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

    public Multicom(PrintWriter out, BufferedReader in, ObjectOutputStream outObject, ObjectInputStream inObject){
        this.out = out;
        this.in = in;
        this.outObject = outObject;
        this.inObject = inObject;
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


    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage echoMessage = new SendMessage();
        echoMessage.setChatId(message.getChatId());


        String text="";
        if(message.hasText()){

             text = message.getText();
        }
        System.out.println(text);




        if (currentState.equals(AvailableStates.INIT) && text.equalsIgnoreCase("!chat")) {
//                    event.getChannel().sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");

//            senderWrapper.sendToMessenger("Initiated chat!\nPlease login[l] or sign in[s]...");

            echoMessage.setText("Initiated chat!\nPlease login[l] or sign in[s]...");
            execute(echoMessage);

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

//                            if(messageFromServer.getType().equals(ServerToClientMessageType.IMAGE)){
//                                SendPhoto photoMessage = new SendPhoto().setPhoto(inputFromServer);
//                                execute(photoMessage);
//                            }else{
//
//                                echoMessage.setText(inputFromServer);
//                                execute(echoMessage);
//                            }



                            ServerToClientMessageType messageType= messageFromServer.getType();
                            System.out.println("tutaj telegramu mesfromserv: "+messageFromServer.getText());
                            if(messageType.equals(ServerToClientMessageType.IMAGE)){
                                SendPhoto photoMessage = new SendPhoto().setPhoto(inputFromServer);
                                execute(photoMessage);

                            }else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {
                                System.out.println("tak");
                                setLoginResultAvailable(true);

                            }else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {
                                System.out.println("nie");
                                setLoginResultAvailable(false);


                            }else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {
                                System.out.println("friend attempt");
                                echoMessage.setText("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                                execute(echoMessage);
                                friendRequest(inputFromServer);

                            }else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){

                                echoMessage.setText("\""+inputFromServer + "\" accepted your friend request");
                                execute(echoMessage);
                            }
                            else {
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


            currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
        }

        else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

            if(text.equalsIgnoreCase("l")){
//                        event.getChannel().sendMessage("You selected login. Input your Username:...");
//                senderWrapper.sendToMessenger("You selected login. Input your Username:...");
                echoMessage.setText("You selected login. Input your Username:...");
                execute(echoMessage);

                currentState = AvailableStates.LOGIN_USERNAME;

            }else if(text.equalsIgnoreCase("s")){
//                        event.getChannel().sendMessage("You selected sign in. Input your Username:...");
//                senderWrapper.sendToMessenger("You selected sign in. Input your Username:...");
                echoMessage.setText("You selected sign in. Input your Username:...");
                execute(echoMessage);

                currentState = AvailableStates.REGISTER_USERNAME;
            }


        }

        else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
            username = text;
            System.out.println(username);
//            senderWrapper.sendToMessenger("Input your Password:...");
            echoMessage.setText("Input your Password:...");
            execute(echoMessage);
            currentState = AvailableStates.LOGIN_PASSWORD;
        }

        else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
            password = text;

            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password,CommunicatorType.MESSENGER));
            System.out.println("Waiting for login result");
            try {
                Thread.yield();
                loginResultAvailable.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Result got!");

            if(loginResult){
                echoMessage.setText("Login successful. Send messages: username#message_text.\n" +
                        "Send images: !image\n" +
                        "Add to friends: !friend\n" +
                        "Create group: !creategroup\n" +
                        "Add user to group: !addtogroup\n" +
                        "Change text sending to group sending: !group. Then groupname#message_text\n" +
                        "Switch to user sending: !user\n" +
                        "Quit: !q");
                execute(echoMessage);

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }else {
                echoMessage.setText("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                execute(echoMessage);

                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }

        }

        else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
            username = text;
            echoMessage.setText("Input your Password:...");
            execute(echoMessage);
            currentState = AvailableStates.REGISTER_PASSWORD;
        }

        else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
            password = text;

            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password,CommunicatorType.MESSENGER));

            try {
                Thread.yield();
                loginResultAvailable.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(loginResult){
                echoMessage.setText("Login successful. Send messages: username#message_text.\\n\" +\n" +
                        "                            \"Send images: !image\\n\" +\n" +
                        "                            \"Add to friends: !friend\\n\" +\n" +
                        "                            \"Create group: !creategroup\\n\" +\n" +
                        "                            \"Add user to group: !addtogroup\\n\" +\n" +
                        "                            \"Change text sending to group sending: !group. Then groupname#message_text\n" +
                        "Switch to user sending: !user\n" +
                        "Quit: !q");
                execute(echoMessage);
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }else {
                echoMessage.setText("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                execute(echoMessage);
                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }
        }

        else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
            if(message.hasPhoto()){
                GetFile getFileRequest = new GetFile();

                getFileRequest.setFileId(message.getPhoto().get(0).getFileId());
                File file = execute(getFileRequest);
                String fileURL = file.getFileUrl("827656409:AAEgFLohXzB9sdkWUIaKz4IaYnAF16dZOrU");
                System.out.println(fileURL);
                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.IMAGE,fileURL,CommunicatorType.TELEGRAM));

            }
            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }

        else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,text,CommunicatorType.MESSENGER));
            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }

        else if(currentState.equals(AvailableStates.CREATE_GROUP)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,text,CommunicatorType.MESSENGER));

            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }

        else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,text,CommunicatorType.MESSENGER));

            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }

        else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){

            if(text.equals("!q")){
                try {
                    TelegramBot.echoSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;

            }else if(text.equalsIgnoreCase("!image")){
                currentState = AvailableStates.IMAGE_SENDING;
                echoMessage.setText("Input image");
                execute(echoMessage);
            }else if(text.equalsIgnoreCase("!friend")){
                currentState  = AvailableStates.ADD_TO_FRIENDS;
                echoMessage.setText("Input friend to add name...");
                execute(echoMessage);
            }else if(text.equalsIgnoreCase("!creategroup")){
                currentState = AvailableStates.CREATE_GROUP;
                echoMessage.setText("Input group name to create...");
                execute(echoMessage);
            }else if(text.equalsIgnoreCase("!addtogroup")){
                currentState  = AvailableStates.ADD_TO_GROUP;
                echoMessage.setText("Input groupname#usertoadd...");
                execute(echoMessage);
            }else if(text.equalsIgnoreCase("!group")){
                isGroupSending = true;
                echoMessage.setText("Group sending!");
                execute(echoMessage);
            }else if(text.equalsIgnoreCase("!user")){
                isGroupSending = false;
                echoMessage.setText("User sending!");
                execute(echoMessage);
            }
            else if(isGroupSending) {
                String []tmpArray = text.split("#");

                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,tmpArray[0]+"#"+username+"#"+tmpArray[1],CommunicatorType.MESSENGER));

            }else{
                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,text,CommunicatorType.MESSENGER));

            }



        }

        else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
            if(text.equalsIgnoreCase("Y")){
                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend,CommunicatorType.MESSENGER));
            }else if(text.equalsIgnoreCase("N")){

            }else {
                echoMessage.setText("Not recognised sign");
                execute(echoMessage);
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
