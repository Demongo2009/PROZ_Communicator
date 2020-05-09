import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class DiscordBot {


    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ClientPrinterThread clientPrinterThread;

    enum AvailableStates{
        INIT,
        CONNECTED_TO_CHAT,
    }

    static AvailableStates currentState = AvailableStates.INIT;

    public static void main(String[] args) {
        // Insert your bot's token here
        String token = "NzA3ODY4MzMxMzk0MjAzNjY5.XrPG8w.tft_u3kOVPIJ4qvFrHZdr2oiluo";

        DiscordApi api = new DiscordApiBuilder().setToken(token).login().join();



        Runtime.getRuntime().addShutdownHook(new ClientShutdownHook(echoSocket));

        Thread thread = new Thread(){
            public void run(){
                String hostName = "localhost";
                int portNumber = 4444;
                try {
                    echoSocket = new Socket(hostName, portNumber);
                    // shutdown hook added for closing the connection if client exits
                    out =
                            new PrintWriter(echoSocket.getOutputStream(), true);
                    in =
                            new BufferedReader(
                                    new InputStreamReader(echoSocket.getInputStream()));


                    clientPrinterThread = new ClientPrinterThread(in);
                    clientPrinterThread.start();

//            send("hello");

                }catch (UnknownHostException e) {
                    e.printStackTrace();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();

        // Add a listener which answers with "Pong!" if someone writes "!ping"
//        api.addMessageCreateListener(event -> {
//            if (event.getMessageContent().equalsIgnoreCase("!ping")) {
//                event.getChannel().sendMessage("Pong!");
//            }
//        });
//
//        api.addMessageCreateListener(event -> {
//            if (event.getMessageContent().equalsIgnoreCase("!elka")) {
//                event.getChannel().sendMessage(" * * * * * elke!");
//            }
//        });


        api.addMessageCreateListener(event -> {
            if (currentState.equals(AvailableStates.INIT) && event.getMessageContent().equalsIgnoreCase("!chat")) {
                event.getChannel().sendMessage("Rozpoczeto czat!");
                out.println("initiate");
                clientPrinterThread.sendEventChannel(event.getChannel());
                clientPrinterThread.releaseMutex();
                currentState = AvailableStates.CONNECTED_TO_CHAT;
            }else if(currentState.equals(AvailableStates.CONNECTED_TO_CHAT) && !event.getMessageAuthor().isBotUser()){
                out.println(event.getMessageContent().toString());
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
