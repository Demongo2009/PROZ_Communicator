package Client;

import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * class to receive ServerToClientMessage
 * */
public class ClientPrinterThread extends Thread {
    private ObjectInputStream inObject;
    private boolean shouldRun;


    ClientPrinterThread(ObjectInputStream in)
    {
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
        if( message == null){
            return;
        }

        Client.notificationsHandler.addNotification(message);
        if( message.getType() == ServerToClientMessageType.LOGOUT){
            this.stopRunning();
        }
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

    public void initializeMessage(String message_text, String userId) {
    }
}