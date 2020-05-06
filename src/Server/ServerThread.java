package Server;

import Server.Protocol;
import Server.ServerPrinterThread;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

public class ServerThread extends Thread{
    static Semaphore mutex;
    ServerSocket serverSocket;
    Socket clientSocket;
    ServerPrinterThread serverPrinterThread;

    ServerThread(ServerSocket serverSocket, Socket clientSocket, ServerPrinterThread serverPrinterThread){
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.serverPrinterThread = serverPrinterThread;
        mutex = new Semaphore(1);
    }



    public void run() {

        PrintWriter out = null;
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));



            String inputLine, outputLine;

            // Initiate conversation with client
            Protocol p = new Protocol(clientSocket.getInetAddress().hashCode(),clientSocket.getPort());
            outputLine = p.processInput(null);
            out.println(outputLine);

            // reading, outputting loop
            while ((inputLine = in.readLine()) != null) {
                if(inputLine == "" || inputLine == "\n"){
                    continue;
                }
                // just printing client message on server out
                mutex.acquire();
                serverPrinterThread.addIncomingMessage(inputLine, clientSocket);
                mutex.release();

                outputLine = p.processInput(inputLine);
                if(outputLine == "" || outputLine == "\n"){
                    continue;
                }
                out.println(outputLine);
                if (outputLine.equals("Bye."))
                    break;
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
