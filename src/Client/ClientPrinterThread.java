package Client;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/*
* class to receive ServerToClientMessage
* */
public class ClientPrinterThread extends Thread {
    ObjectInputStream inObject;
    boolean shouldRun;

    ClientPrinterThread(ObjectInputStream in){
        this.inObject = in;
        shouldRun = true;
    }

    private ServerToClientMessage receiveMessage(){
        ServerToClientMessage message = null;

        try {
            message = (ServerToClientMessage)inObject.readObject();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return message;
    }

    private void processMessage(ServerToClientMessage message){
        //TODO: we need to handle these messages

        //todo add user when we get USER_ACCEPTED_YOUR_FRIEND_REQUEST,
        //todo add group when we get USER_ADDED_YOU_TO_GROUP,
        //todo and more probably...
        if( message == null){
            return;
        }
        if( message.getType() == ServerToClientMessageType.LOGOUT){
            this.stopRunning();
            return;
        }
        Client.notificationsHandler.addNotification(message);
        System.out.println( message.getText() );

    }

    public void run(){
        while(shouldRun) {
            processMessage(receiveMessage());
        }
        //System.out.println("Koncze sluchac");
    }

    void stopRunning(){
        shouldRun = false;
    }
}
