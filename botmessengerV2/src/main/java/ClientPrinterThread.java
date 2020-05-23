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

public class ClientPrinterThread extends Thread {

    ObjectInputStream inObject;

    ClientPrinterThread(ObjectInputStream inObject){

        this.inObject = inObject;

        mutex = new Semaphore(0);

    }

    public void initializeMessage( String text, String userId){
        this.text = text;
        this.userId = userId;
    }
    String text;
    String userId;

    public void initializePlatform(){

    }


    static Semaphore mutex;

    public void releaseMutex(){
        mutex.release();
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



    public void run(){
        try{
            String inputFromServer;
            mutex.acquire();
            ServerToClientMessage message = null;
            while((message = receiveMessage()) != null){
                inputFromServer = message.getText();
                if(inputFromServer == "" || inputFromServer == "\n"){
                    continue;
                }
//                System.out.println("Server.Server: "+ inputFromServer);



                ServerToClientMessageType messageType= message.getType();
                if(messageType.equals(ServerToClientMessageType.IMAGE)){
//                    message_tpl.setAttachment("image",inputFromServer,true);
                    MessengerBot.sendRegularMessage(inputFromServer);

                }else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {
                    System.out.println("tak");
                    MessengerBot.setLoginResultAvailable(true);

                }else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {
                    System.out.println("nie");
                    MessengerBot.setLoginResultAvailable(false);


                }else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {
                    System.out.println("friend attempt");
//                    message_tpl.setMessageText("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                    MessengerBot.sendRegularMessage("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
                    MessengerBot.friendRequest(inputFromServer);

                }else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){
//                    message_tpl.setMessageText("\""+inputFromServer + "\" accepted your friend request");
                    MessengerBot.sendRegularMessage("\""+inputFromServer + "\" accepted your friend request");
                }
                else {
//                    message_tpl.setMessageText(inputFromServer);
                    MessengerBot.sendRegularMessage("\""+inputFromServer + "\" accepted your friend request");
                }

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
