package com.clivern.dunk;

import java.io.IOException;
import java.net.Socket;

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
