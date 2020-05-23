
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

public class ClientPrinterThread extends Thread {
    private BufferedReader in;
    private ObjectInputStream inObject;
    TextChannel textChannel;


    ClientPrinterThread(BufferedReader in, ObjectInputStream inObject){
        this.in=in;
        this.inObject = inObject;

    }

    public void sendEventChannel(TextChannel textChannel){
        this.textChannel = textChannel;
    }

    // Receiving message form server
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
                inputFromServer = message.getText();

                if(inputFromServer.equals("") || inputFromServer.equals("\n")){
                    continue;
                }

                ServerToClientMessageType messageType= message.getType();


                // Depending on message type different messages will be sent
                // Image
                if(messageType.equals(ServerToClientMessageType.IMAGE)){
                    new MessageBuilder().addAttachment(new URL(inputFromServer)).send(textChannel);

                }
                // Confirm login
                else if(messageType.equals(ServerToClientMessageType.CONFIRM_LOGIN)) {


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


        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
