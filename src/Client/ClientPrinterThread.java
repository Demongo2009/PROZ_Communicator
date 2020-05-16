package Client;

import Messages.clientToServer.ClientToServerMessage;
import Messages.clientToServer.ClientToServerMessageType;
import Messages.serverToClient.ServerToClientMessage;

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

    private void processMessage(ServerToClientMessage message)
    {
        if( message == null){
            return; }
        Client.notificationsHandler.addNotification(message);
        //try {
        //    switch (message.getType()) {
        //       case USER_WANTS_TO_BE_YOUR_FRIEND:
        //            System.out.println("USER WANTS TO BE YOUR FRIEND");
        //            /*
        //             * TUTAJ BĘDZIE TRZEBA ZROBIĆ POWIADOMIENIE, KTORE PO WYKONANIU ZWOLNI WĄTEK
        //             * if yes
        //             *      save his nickname to 'friends' arrayList and send to server confirmation
        //             * if no
        //             *      do nothing
        //             */
        //            break;
        //        default:
        //            throw new Exception("Invalid message from server received");
        //    }
        //}catch (Exception e){
        //    e.printStackTrace();
        //}

    }

    void confirmFriendship(String newFriend){
        /*for(String s: friends){
            if( s.equals(newFriend)){
                System.out.println("USER IS ALREADY YOUR FRIEND");//should never be executed in normal circumnstances
                return;
            }
        }

        friends.add(newFriend);
        ClientToServerMessageType type = ClientToServerMessageType.CONFIRMATION_OF_FRIENDSHIP;
        ClientToServerMessage message = new ClientToServerMessage(type, newFriend);
*/

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
