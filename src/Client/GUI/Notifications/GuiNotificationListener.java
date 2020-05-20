package Client.GUI.Notifications;

import Client.Client;
import Client.GUI.Main.MainWindow;
import Client.NotificationsHandler;
import Messages.serverToClient.ServerToClientMessage;

import java.awt.*;

public class GuiNotificationListener extends Thread
{
    private boolean shouldRun=true;
    private MainWindow mainWindow;
    NotificationsHandler notificationsHandler;
    public GuiNotificationListener(MainWindow ref)
    {
        mainWindow=ref;
        notificationsHandler=Client.notificationsHandler;
    }

    public void run()
    {
        while (shouldRun)
        {
            handleNotification();
        }
    }

    private void handleNotification()
    {

        ServerToClientMessage notification = notificationsHandler.getNotification();
        switch (notification.getType())
        {
            case LOGOUT:
                shouldRun=false;
                break;

            case GROUP_NAME_OCCUPIED:
                mainWindow.setAlert(Color.RED,"Group already exists!");
                break;

            case USER_IS_NOT_CONNECTED:
                mainWindow.serverAlert(notification.getText(),"USER IS NOT CONNECTED");
                break;

            case TEXT_MESSAGE_FROM_USER:
                mainWindow.getMessageFromUser(notification.getText());
                break;

            case TEXT_MESSAGE_FROM_GROUP:
                mainWindow.getMessageFromGroup(notification.getText());
                break;

            case USER_ADDED_YOU_TO_GROUP:
                Client.groups.add(notification.getText());
                mainWindow.receiveGroupInvitation(notification.getText());
                mainWindow.refresh();
                break;

            case USER_WANTS_TO_BE_YOUR_FRIEND:
                mainWindow.receiveFriendRequest(notification.getText());
                break;

            case USER_ACCEPTED_YOUR_FRIEND_REQUEST:
                Client.friends.add(notification.getText());
                mainWindow.refresh();
                break;

            default:
                System.out.println("no niestety nie udalo sie obsluzyc poprawnie tego");

        }
    }

}
