import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;


/**
 * Class handling Telegram Bot and connection with server initialization.
 */
public class TelegramBot {

    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ArrayList<String> userIdArray;
    static ObjectOutputStream outObject;
    static ObjectInputStream inObject;


    public static void main(String[] args) {
        userIdArray= new ArrayList<>();

        // Setup connection with server
        String hostName = "localhost";
        int portNumber = 9999;
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

        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        // Api initialization
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Multicom(out,in,outObject,inObject));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
