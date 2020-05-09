package Client;

import java.io.IOException;
import java.net.Socket;

public class ClientShutdownHook extends Thread {

    Socket clientSocket;
    ClientShutdownHook(Socket clientSocket){
        this.clientSocket = clientSocket;
    }
/*
* should send communicate to server to close thread
* */
    @Override
    public void run() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
