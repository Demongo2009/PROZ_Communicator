import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class TelegramBot {

    static Socket echoSocket;
    static PrintWriter out;
    static BufferedReader in;
    static ArrayList<String> userIdArray;


    public static void main(String[] args) {
        userIdArray= new ArrayList<>();


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


        }catch (UnknownHostException e) {
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }

        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new Multicom(out,in));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
