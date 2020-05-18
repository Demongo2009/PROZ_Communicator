import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Server.CommunicatorType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.javacord.api.entity.message.MessageAttachment;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.Semaphore;

public class DiscordBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;
    static ObjectOutputStream outObject;
    static ObjectInputStream inObject;
    static String username;
    static String password;
    public static Semaphore loginResultAvailable;
    public static boolean loginResult;

    enum AvailableStates{
        INIT,
        CONNECTED_TO_CHAT,
        SELECT_LOGIN_OR_REGISTER,
        LOGIN_USERNAME,
        LOGIN_PASSWORD,
        REGISTER_USERNAME,
        REGISTER_PASSWORD,
        WAIT_FOR_LOGIN_RESULT
    }

    static AvailableStates currentState = AvailableStates.INIT;



    static private void sendMessage(ClientToServerMessage message){
        try {
            outObject.writeObject( message );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.XsKA2Q.sCpCCLwzuRtv9_tIb6Oo3p0ElXU";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();


        loginResultAvailable = new Semaphore(0);

        Thread thread = new Thread(){
            public void run(){
                String hostName = "localhost";
                int portNumber = 4444;
                try {
                    echoSocket = new Socket(hostName, portNumber);
                    Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));

                    // shutdown hook added for closing the connection if client exits
                    out =
                            new PrintWriter(echoSocket.getOutputStream(), true);
                    in =
                            new BufferedReader(
                                    new InputStreamReader(echoSocket.getInputStream()));



                    outObject = new ObjectOutputStream( echoSocket.getOutputStream()) ;
                    inObject = new ObjectInputStream( echoSocket.getInputStream() );

                    clientPrinterThread = new ClientPrinterThread(in,inObject);
                    clientPrinterThread.start();

                }catch (UnknownHostException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();




        api.addMessageCreateListener(event -> {
            if(event.getMessageAuthor().isBotUser()){
                return;
            }
            if (currentState.equals(AvailableStates.INIT) && event.getMessageContent().equalsIgnoreCase("!chat")) {
                event.getChannel().sendMessage("Initiated chat!\nPlease login[l] or sign in[s]...");
                clientPrinterThread.sendEventChannel(event.getChannel());

                currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
            }

            else if(currentState.equals(AvailableStates.SELECT_LOGIN_OR_REGISTER)){

                if(event.getMessageContent().equalsIgnoreCase("l")){
                    event.getChannel().sendMessage("You selected login. Input your Username:...");
                    currentState = AvailableStates.LOGIN_USERNAME;

                }else if(event.getMessageContent().equalsIgnoreCase("s")){
                    event.getChannel().sendMessage("You selected sign in. Input your Username:...");
                    currentState = AvailableStates.REGISTER_USERNAME;
                }


//                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.DISCORD));
//
//                clientPrinterThread.sendEventChannel(event.getChannel());
//                clientPrinterThread.releaseMutex();
//
//                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }

            else if(currentState.equals(AvailableStates.LOGIN_USERNAME)){
                username = event.getMessageContent();
                System.out.println(username);
                event.getChannel().sendMessage("Input your Password:...");
                currentState = AvailableStates.LOGIN_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.LOGIN_PASSWORD)){
                password = event.getMessageContent();

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,username+"#"+password,CommunicatorType.DISCORD));

                currentState = AvailableStates.WAIT_FOR_LOGIN_RESULT;
                System.out.println("gere");
                try {
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("here");
            }

            else if(currentState.equals(AvailableStates.REGISTER_USERNAME)){
                username = event.getMessageContent();
                event.getChannel().sendMessage("Input your Password:...");
                currentState = AvailableStates.REGISTER_PASSWORD;
            }

            else if(currentState.equals(AvailableStates.REGISTER_PASSWORD)){
                password = event.getMessageContent();

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_REGISTER,username+"#"+password,CommunicatorType.DISCORD));
                currentState = AvailableStates.WAIT_FOR_LOGIN_RESULT;
                try {
                    loginResultAvailable.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else if(currentState.equals(AvailableStates.WAIT_FOR_LOGIN_RESULT)){


                if(loginResult){
                    event.getChannel().sendMessage("Login successful.");
                    currentState = AvailableStates.CONNECTED_TO_CHAT;
                }else {
                    event.getChannel().sendMessage("Incorrect data. Try again.\nPlease login[l] or sign in[s]...");
                    currentState = AvailableStates.SELECT_LOGIN_OR_REGISTER;
                }

            }

            else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT)){


                sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT_TO_USER,event.getMessageContent().toString(),CommunicatorType.DISCORD));
                if(event.getMessageAttachments().size() > 0 ){
                    List<MessageAttachment> attachmentArray = event.getMessage().getAttachments();
                    if(attachmentArray.get(0)!=null) {
                        sendMessage(new ClientToServerMessage(ClientToServerMessageType.IMAGE,attachmentArray.get(0).getUrl().toString(), CommunicatorType.DISCORD));
                    }

                }

            }
            else{
                if (event.getMessageContent().equalsIgnoreCase("!ping")) {
                    event.getChannel().sendMessage("Pong!");
                }
                if (event.getMessageContent().equalsIgnoreCase("!elka")) {
                    event.getChannel().sendMessage(" * * * * * elke!");
                }
            }



        });


        // Print the invite url of your bot
        System.out.println("You can invite the bot by using the following url: " + api.createBotInvite());
    }


}
