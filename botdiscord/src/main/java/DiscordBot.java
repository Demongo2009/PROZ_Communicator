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

public class DiscordBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;
    static ObjectOutputStream outObject;
    static ObjectInputStream inObject;

    enum AvailableStates{
        INIT,
        CONNECTED_TO_CHAT,
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
        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.Xr-iIA.G4x6ie7HdZlRTbd4GX6BvKsK5VY";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();




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
            if (currentState.equals(AvailableStates.INIT) && event.getMessageContent().equalsIgnoreCase("!chat")) {
                event.getChannel().sendMessage("Rozpoczeto czat!");

                sendMessage(new ClientToServerMessage(ClientToServerMessageType.REQUEST_LOGIN,"login#password", CommunicatorType.DISCORD));

                clientPrinterThread.sendEventChannel(event.getChannel());
                clientPrinterThread.releaseMutex();

                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT) && !event.getMessageAuthor().isBotUser()){

                if(event.getMessageAttachments() != null){
                    List<MessageAttachment> attachmentArray = event.getMessage().getAttachments();
                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.IMAGE,attachmentArray.get(0).getUrl().toString(),CommunicatorType.DISCORD));

                }else {

                    sendMessage(new ClientToServerMessage(ClientToServerMessageType.TEXT,"discord#"+event.getMessageContent().toString(),CommunicatorType.DISCORD));
                }
                clientPrinterThread.sendEventChannel(event.getChannel());

            }else{
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
