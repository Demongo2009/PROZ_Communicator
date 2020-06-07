import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

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

/**
 * Main Telegram Bot class.
 * Handles TelegramBots API specific message receiving and sending.
 */
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

    /**
     * Available states of bot state machine.
     */
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

    /**
     * Function realising mutex when asynchronous result is available.
     * @param result type of result
      */
    void setLoginResultAvailable(boolean result){
        loginResult = result;
        loginResultAvailable.release();
    }

    /**
     * Function sending message to server.
     * @param message message to be sent
      */
    void sendMessageToServer(ClientToServerMessage message){
        try {
            outObject.writeObject(message);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Function handling asynchronous friend request.
     * @param friendName name of the friend that sent the request
      */
    static public void friendRequest(String friendName){
        currentState = AvailableStates.FRIEND_REQUEST_PENDING;
        friend = friendName;
    }

    /**
     * Class construction.
     * @param out for sending strings to socket
     * @param in for receiving strings form socket
     * @param outObject for sending messages to server
     * @param inObject for receiving messages from server
     */
    public Multicom(PrintWriter out, BufferedReader in, ObjectOutputStream outObject, ObjectInputStream inObject){
        this.out = out;
        this.in = in;
        this.outObject = outObject;
        this.inObject = inObject;
    }

    /**
     * Function receiving message from server.
      */
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


    /**
     * Telegram Api specific event for receiving message.
     * @param update new message
      */

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


    /**
     * Most important function of bot designed to handling incoming message.
     * Implemented with state machine.
      */

    private void handleIncomingMessage(Message message) throws TelegramApiException {
        SendMessage echoMessage = new SendMessage();
        echoMessage.setChatId(message.getChatId());


        String text="";
        if(message.hasText()){

             text = message.getText();
        }




        // Bot state machine
        if (currentState.equals(AvailableStates.INIT) && text.equalsIgnoreCase("!chat")) {

            initialState(echoMessage);

        }
        // Select login or register
        else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

            selectLoginOrRegister(echoMessage, text);


        }
        // Login username
        else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
            username = text;

            echoMessage.setText("Input your Password:...");
            execute(echoMessage);
            currentState = AvailableStates.LOGIN_PASSWORD;
        }
        // Login password
        else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){

            loginPassword(echoMessage, text);

        }
        // Register username
        else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
            username = text;
            echoMessage.setText("Input your Password:...");
            execute(echoMessage);
            currentState = AvailableStates.REGISTER_PASSWORD;
        }
        // Register password
        else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
            registerPassword(echoMessage, text);
        }


        // Connected to chat, now choose options
        else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){
            connectedToChat(echoMessage, text);


        }

        // Image sending
        else if(currentState.equals(AvailableStates.IMAGE_SENDING)){
            if(message.hasPhoto()){
                GetFile getFileRequest = new GetFile();

                getFileRequest.setFileId(message.getPhoto().get(0).getFileId());
                File file = execute(getFileRequest);
                String fileURL = file.getFileUrl("827656409:AAEgFLohXzB9sdkWUIaKz4IaYnAF16dZOrU");
                System.out.println(fileURL);
                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.IMAGE,fileURL));

            }
            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }
        // Add to friends
        else if(currentState.equals(AvailableStates.ADD_TO_FRIENDS)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_FRIENDS,text));
            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }
        // Create group
        else if(currentState.equals(AvailableStates.CREATE_GROUP)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CREATE_GROUP,text));

            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }
        // Add to group
        else if(currentState.equals(AvailableStates.ADD_TO_GROUP)){
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.ADD_USER_TO_GROUP,text));

            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }
        // Friend request pending
        else if(currentState.equals(AvailableStates.FRIEND_REQUEST_PENDING)){
            if(text.equalsIgnoreCase("Y")){
                sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP,friend));
            }else if(text.equalsIgnoreCase("N")){

            }else {
                echoMessage.setText("Not recognised sign");
                execute(echoMessage);
            }
            currentState = AvailableStates.CONNECTED_TO_CHAT;
        }

    }

    /**
     * Function that is responsible for handling messages if client is already connected.
     * @param echoMessage message to be sent
     * @param text client text
     * @throws TelegramApiException exception
     */
    private void connectedToChat(SendMessage echoMessage, String text) throws TelegramApiException {
        // Quit
        if(text.equals("!q")){
            try {
                TelegramBot.echoSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;

        }
        // Image
        else if(text.equalsIgnoreCase("!image")){
            currentState = AvailableStates.IMAGE_SENDING;
            echoMessage.setText("Input image");
            execute(echoMessage);
        }
        // Friend add
        else if(text.equalsIgnoreCase("!friend")){
            currentState  = AvailableStates.ADD_TO_FRIENDS;
            echoMessage.setText("Input friend to add name...");
            execute(echoMessage);
        }
        // Create group
        else if(text.equalsIgnoreCase("!creategroup")){
            currentState = AvailableStates.CREATE_GROUP;
            echoMessage.setText("Input group name to create...");
            execute(echoMessage);
        }
        // Add to group
        else if(text.equalsIgnoreCase("!addtogroup")){
            currentState  = AvailableStates.ADD_TO_GROUP;
            echoMessage.setText("Input groupname#usertoadd...");
            execute(echoMessage);
        }
        // Group sending
        else if(text.equalsIgnoreCase("!group")){
            isGroupSending = true;
            echoMessage.setText("Group sending!");
            execute(echoMessage);
        }
        // User sending
        else if(text.equalsIgnoreCase("!user")){
            isGroupSending = false;
            echoMessage.setText("User sending!");
            execute(echoMessage);
        }
        // Message sending
        else if(isGroupSending) {
            String []tmpArray = text.split("#");

            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_GROUP,tmpArray[0]+"#"+username+"#"+tmpArray[1]));

        }else{
            sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,text));

        }
    }

    /**
     * Function of state machine handling registration process. Particularly password part.
     * @param echoMessage message to be sent
     * @param text text from client
     * @throws TelegramApiException exception
     */
    private void registerPassword(SendMessage echoMessage, String text) throws TelegramApiException {
        password = text;

        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password));

        // Asynchronous result
        try {
            Thread.yield();
            loginResultAvailable.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(loginResult){
            echoMessage.setText("Login successful. Send messages: username#message_text. \n" +
                    "Send images: !image.\n" +
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

    /**
     * Function of state machine handling login process. Particularly password part.
     * @param echoMessage message to be sent
     * @param text text from client
     * @throws TelegramApiException exception
     */
    private void loginPassword(SendMessage echoMessage, String text) throws TelegramApiException {
        password = text;

        sendMessageToServer(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password));

        // Asynchronous result
        try {
            Thread.yield();
            loginResultAvailable.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


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

    /**
     * Function of state machine handling selection of login and register.
     * @param echoMessage message to be sent
     * @param text text from client
     * @throws TelegramApiException exception
     */
    private void selectLoginOrRegister(SendMessage echoMessage, String text) throws TelegramApiException {
        if(text.equalsIgnoreCase("l")){

            echoMessage.setText("You selected login. Input your Username:...");
            execute(echoMessage);

            currentState = AvailableStates.LOGIN_USERNAME;

        }else if(text.equalsIgnoreCase("s")){

            echoMessage.setText("You selected sign in. Input your Username:...");
            execute(echoMessage);

            currentState = AvailableStates.REGISTER_USERNAME;
        }
    }

    /**
     * Function of state machine handling initailization of thread receiving messages form server.
     * It is implemented here because Telegram API requires it.
     * @param echoMessage message to be sent
     * @throws TelegramApiException exception
     */
    private void initialState(SendMessage echoMessage) throws TelegramApiException {
        echoMessage.setText("Initiated chat!\nPlease login[l] or sign in[s]...");
        execute(echoMessage);

        // Thread for asynchronous messages
        Thread thread = new Thread(){
            @Override
            public void run() {

                try {
                    String inputFromServer;
                    ServerToClientMessage messageFromServer = null;

                    // Waiting for message form server
                    while ((messageFromServer = receiveMessage()) != null) {
                        inputFromServer = messageFromServer.getText();
                        if (inputFromServer.equals("") || inputFromServer.equals("\n")) {
                            continue;
                        }

                        ServerToClientMessageType messageType= messageFromServer.getType();


                        // Choosing which message should be send and special actions taken
                        // Image
                        if(messageType.equals(ServerToClientMessageType.IMAGE)){
                            SendPhoto photoMessage = new SendPhoto().setPhoto(inputFromServer);
                            execute(photoMessage);

                        }
                        // Confirm login
                        else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {

                            // Sending user friends and groups
                            String[] friendsAndGroups = inputFromServer.split("@");
                            String[] friends = friendsAndGroups[0].split("#");
                            String[] groups = friendsAndGroups[1].split("#");
                            String friendsText= "";
                            if(friends.length>0){

                                for (String f: friends){
                                    if(f!=null)
                                        friendsText += ", "+f;
                                }
                            }
                            String groupsText = "";
                            if(groups.length>0){

                                for (String g: groups){
                                    if(g!=null)
                                        groupsText += ", "+g;
                                }
                            }

                            echoMessage.setText("Your friends are: "+friendsText+".\nYour groups are: "+groupsText+".");
                            execute(echoMessage);

                            setLoginResultAvailable(true);

                        }
                        // Reject login
                        else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {

                            setLoginResultAvailable(false);


                        }
                        // Someone send friend request
                        else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {

                            echoMessage.setText("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                            execute(echoMessage);
                            friendRequest(inputFromServer);

                        }
                        // Friend request accepted
                        else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){

                            echoMessage.setText("\""+inputFromServer + "\" accepted your friend request");
                            execute(echoMessage);
                        }
                        // Normal message
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


    /**
     * Telegram API specific function for setting name of bot.
     * @return bot name
     */
    public String getBotUsername() {
        return "MultiComEitiBot";
    }

    /**
     * Telegram API specific function for setting token.
     * @return token
     */
    public String getBotToken() {
        return "827656409:AAEgFLohXzB9sdkWUIaKz4IaYnAF16dZOrU";
    }


}
