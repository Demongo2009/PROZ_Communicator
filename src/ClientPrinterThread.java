import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientPrinterThread extends Thread {
    BufferedReader in;

    ClientPrinterThread(BufferedReader in){
        this.in=in;
    }

    public void run(){
        try{
            String inputFromServer;
            while((inputFromServer = in.readLine()) != null){
                System.out.println("Server: "+ inputFromServer);
            }

        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
