package Client;

import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;
import com.sun.nio.sctp.NotificationHandler;

import java.util.ArrayList;

/*
* Nie wiem jeszcze jakby to miało działać
* Czy GUI  powinno coś podświetlić po otrzymaniu powiadomienia, po czym powinno zostać zdjęte
* */
/*

* */
public class NotificationsHandler
{

    ArrayList<ServerToClientMessage> notifications = new ArrayList<ServerToClientMessage>();
    public NotificationsHandler(){};

    void addNotification(ServerToClientMessage message)
    {
        notifications.add( message );
    }

    /* removes first notification and returns it*/
    ServerToClientMessage getNotification()
    {
        ServerToClientMessage notification = notifications.get(0);
        notifications.remove(0);
        return notification;
    }

}
