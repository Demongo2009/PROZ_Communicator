package Server;

import javafx.util.Pair;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Semaphore;
/*
public class ServerPrinterThread extends Thread {

//    String incomingMessage;
//    String clientName;
    LinkedList< Pair<String, String> > buffer;
    Semaphore incomingMessageMutex;
    Semaphore mutex;

    ServerPrinterThread(){
        incomingMessageMutex = new Semaphore(0);
        mutex = new Semaphore(1);
        buffer = new LinkedList<>();
    }

    public void addIncomingMessage(String inputLine, Socket clientSocket){
        buffer.add( new Pair<String, String>(clientSocket.toString(),inputLine) );
        incomingMessageMutex.release();
    }

    private boolean checkIfIncomingMessage() throws InterruptedException {
        incomingMessageMutex.acquire();

        return true;
    }

    public void run(){
        try{
            while(checkIfIncomingMessage()){
                mutex.acquire();
                Pair<String, String> pair= buffer.remove();
                String clientName = pair.getKey();
                String incomingMessage = pair.getValue();
                System.out.println("Client.Client ( " + clientName + " ): " + incomingMessage);
                mutex.release();
            }

        }catch (InterruptedException e){
            e.printStackTrace();
        }
    }
}
*/