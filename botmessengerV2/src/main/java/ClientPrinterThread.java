import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import com.clivern.racter.BotPlatform;
import com.clivern.racter.senders.templates.*;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.net.URL;
import java.util.concurrent.Semaphore;

//import com.clivern.racter.BotPlatform;

/**
 * Class responsible for handling messages form server.
 * Designed for Messenger Bot.
 */

public class ClientPrinterThread extends Thread {

    ObjectInputStream inObject;
    static Semaphore mutex;



    /**
     * Constructor for ClientPrinterThread.
     * @param inObject object input stream for receiving messages
     */
    ClientPrinterThread(ObjectInputStream inObject){
        this.inObject = inObject;
        mutex = new Semaphore(0);
    }


    /**
     * Function releasing mutex when object can get message.
     */
    public void releaseMutex(){
        mutex.release();
    }

    /**
     * Function responsible for receive message from server.
      */
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



    public void run(){
        try{
            String inputFromServer;
            mutex.acquire();
            ServerToClientMessage message = null;

            // Waiting for asynchronous message
            while((message = receiveMessage()) != null){
                inputFromServer = message.getText();
                if(inputFromServer == "" || inputFromServer == "\n"){
                    continue;
                }




                ServerToClientMessageType messageType= message.getType();
                // Switching for different message type
                // Image
                if(messageType.equals(ServerToClientMessageType.IMAGE)){

                    MessengerBot.sendRegularMessage(inputFromServer);

                }
                // Confirm login
                else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {

                    MessengerBot.setLoginResultAvailable(true);

                }
                // Reject login
                else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {

                    MessengerBot.setLoginResultAvailable(false);


                }
                // Someone wants to be your friend
                else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {

                    MessengerBot.sendRegularMessage("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                    MessengerBot.friendRequest(inputFromServer);

                }
                // Accepted friend request
                else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){

                    MessengerBot.sendRegularMessage("\""+inputFromServer + "\" accepted your friend request");
                }
                else {

                    MessengerBot.sendRegularMessage("\""+inputFromServer + "\" accepted your friend request");
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}