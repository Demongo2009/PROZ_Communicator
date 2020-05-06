package Client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/*
public class ClientPrinterThread extends Thread {
    BufferedReader in;
    ApplicationClient app;

    ClientPrinterThread(BufferedReader in, ApplicationClient app){
        this.in=in;
        this.app = app;
    }

    public void run(){
        try{
            String inputFromServer;
            while((inputFromServer = in.readLine()) != null){
                if(inputFromServer == "" || inputFromServer == "\n"){
                    continue;
                }
                System.out.println("Server.Server: "+ inputFromServer);
                app.updateConversationText(inputFromServer);


            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
*/