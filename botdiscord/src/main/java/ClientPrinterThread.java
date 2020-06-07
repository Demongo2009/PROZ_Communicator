
import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.MessageBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Semaphore;

/**
 * Class assigned to handling asynchronous messages coming from server.
 * Designed specially for Discord.
 *
 */

public class ClientPrinterThread extends Thread {

    private ObjectInputStream inObject;
    TextChannel textChannel;

    /**
     * Constructor for ClientPrinterThread
     * @param inObject object input stream for receiving messages
     */
    ClientPrinterThread(ObjectInputStream inObject){

        this.inObject = inObject;

    }

    /**
     * Function assigning textChannel required for sending message to specific client
     * @param textChannel textChannel of client
     */
    public void sendEventChannel(TextChannel textChannel){
        this.textChannel = textChannel;
    }

    /**
     * Function which purpose is receiving message form server.
     *
     * @return message got from server
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
            ServerToClientMessage message = null;


            // Wait for asynchronous messages
            while((message = receiveMessage()) != null){
                handleMessage(message);
            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Function responsible for handling message from server.
     * @param message message form server
     * @throws MalformedURLException exception
     */
    private void handleMessage(ServerToClientMessage message) throws MalformedURLException {
        String inputFromServer;
        inputFromServer = message.getText();

        if(inputFromServer.equals("") || inputFromServer.equals("\n")){
            return;
        }

        ServerToClientMessageType messageType= message.getType();


        // Depending on message type different messages will be sent
        // Image
        if(messageType.equals(ServerToClientMessageType.IMAGE)){
            new MessageBuilder().addAttachment(new URL(inputFromServer)).send(textChannel);

        }
        // Confirm login
        else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {

            confirmLogin(inputFromServer);

        }
        // Reject login
        else if(messageType.equals(ServerToClientMessageType.REJECT_LOGIN)) {

            DiscordBot.setLoginResultAvailable(false);

        }
        // Someone wants to be a friend
        else if(messageType.equals(ServerToClientMessageType.USER_WANTS_TO_BE_YOUR_FRIEND)) {

            textChannel.sendMessage("User \""+inputFromServer+"\" wants to be your friend. [Y] accept [N] refuse");
            DiscordBot.friendRequest(inputFromServer);

        }
        // Friend accepted your request
        else if(messageType.equals(ServerToClientMessageType.USER_ACCEPTED_YOUR_FRIEND_REQUEST)){
            textChannel.sendMessage("\""+inputFromServer + "\" accepted your friend request");
        }
        else {
            textChannel.sendMessage(inputFromServer);
        }
    }

    /**
     * Function responsible for handling login confirmation.
     * @param inputFromServer text from server
     */
    private void confirmLogin(String inputFromServer) {
        // Printing friends and groups
        String[] friendsAndGroups = inputFromServer.split("@");
        String[] friends = friendsAndGroups[0].split("#");
        String[] groups = friendsAndGroups[1].split("#");

        String friendsText = "";
        if(friends.length>0){

            for (String f: friends){

                    friendsText += ", "+f;
            }
        }

        String groupsText = "";
        if(groups.length>0){


            for (String g: groups){

                    groupsText += ", "+g;
            }
        }

        textChannel.sendMessage("Your friends are: "+friendsText+".\nYour groups are: "+groupsText+".");

        DiscordBot.setLoginResultAvailable(true);
    }
}
