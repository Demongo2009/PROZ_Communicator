import java.io.IOException;
import java.net.Socket;


/**
 * Class intended to close connection in good manner.
 * No not used, because not working with bots as intended.
 * @deprecated
 */
public class ClientShutdownHook extends Thread {

    Socket clientSocket;
    ClientShutdownHook(Socket clientSocket){
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
