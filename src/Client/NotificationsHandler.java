package Client;

import Messages.serverToClient.ServerToClientMessage;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Class to receive messages from server and to pass them to GUI
 * */
public class NotificationsHandler
{
    /** in order not to read from empty array list*/
    private Semaphore sem = new Semaphore(0);
    private ArrayList<ServerToClientMessage> notifications = new ArrayList<ServerToClientMessage>();

    /**
     * @param message message to add to ArrayList
     * releases Semaphore
     * */
    void addNotification(ServerToClientMessage message)
    {
        notifications.add( message );
        sem.release();
    }

    /** removes first notification and returns it
     * acquires Semaphore
     * */
    public ServerToClientMessage getNotification()
    {
        ServerToClientMessage notification;
        try
        {
            sem.acquire();

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