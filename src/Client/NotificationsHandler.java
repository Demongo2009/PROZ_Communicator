package Client;

import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import com.sun.nio.sctp.NotificationHandler;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class NotificationsHandler
{
    private Semaphore mutex = new Semaphore(0);
    ArrayList<ServerToClientMessage> notifications = new ArrayList<ServerToClientMessage>();
    public NotificationsHandler(){};

    void addNotification(ServerToClientMessage message)
    {
        notifications.add( message );
        mutex.release();
    }

    /* removes first notification and returns it*/
    public ServerToClientMessage getNotification()
    {
        ServerToClientMessage notification;
        try
        {
            mutex.acquire();

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        notification = notifications.get(0);
        notifications.remove(0);
        return notification;
    }
}