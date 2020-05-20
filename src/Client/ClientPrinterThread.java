package Client;

import Messages.serverToClient.ServerToClientMessage;
import Messages.serverToClient.ServerToClientMessageType;

import java.io.IOException;
import java.io.ObjectInputStream;

/*
 * class to receive ServerToClientMessage
 * */
public class ClientPrinterThread extends Thread {
    ObjectInputStream inObject;
    boolean shouldRun;


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
            return;
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
}

/*
 * Musimy ogarnąć te komunikaty, poniżej wypiszę to jakie komunikaty przychodzą i jaką treść za sobą niosą jakbyś chciał powoli zacząć się w to bawić:
 * będę wypisywał je w takiej formie:
 *
 * NAZWA_KOMUNIKATU
 * tresc_wiadomosci
 * //komentarz to komunikatu, co trzeba zrobić itp itd
 *
 *
 *
 *
 * 1.USER_WANTS_TO_BE_YOUR_FRIEND
 * nickname_osoby_która_chcę_cie_dodac_do_znajomych
 * // po otrzymaniu tego komuniaktu fajnie by bylo jakby podświetliła się zakładka "Add friends", a w niej pojawiło się okno dialogowe TAK/NIE
 * // jeżeli zaakceptujemy zaproszenie to powinna zostać wywołana funkcja klienta confirmFriendship(nickname), gdzie nickname to użytkownik, od którego zaakceptowaliśmy zaproszenie
 * // jezeli NIE zaakceptujemy to nic nie robimy
 *
 *
 * 2.USER_ACCEPTED_YOUR_FRIEND_REQUEST
 * nickname_osoby_ktora_zaakceptowała_nasze_zaproszenie
 * // po otrzymaniu musimy dodać do tablicy znajmoych klienta nowego znajomego, czyli friends.add(nickname)
 * // oraz podświetlić zakładke "Friends", ze coś takiego miało miejsce
 *
 *
 * 3.TEXT_MESSAGE_FROM_USER
 * nickname # treść_wiadomości
 * // po otrzymaniu powinna pojawić się nowa wiadomość w odpowiednim panelu rozmowy
 *
 *
 * 4.USER_IS_NOT_CONNECTED
 * nickname
 * // to będzie wiadomość od serwera, którą otrzymamy, gdy użytkownik do którego piszemy nie jest zalogowany.
 * // Fajnie by było jakby w naszym panelu rozmowy pojawił się stosowny tekst komunikujący to
 *
 * 5.GROUP_NAME_OCCUPIED
 * group_name
 * // jak bedziemy chcieli założyć grupę, która już istnieje
 *
 *
 * 6.USER_ADDED_YOU_TO_GROUP
 * group_name
 * // po tym komunikacie musimy dodać nazwe grupy analogicznie jak to było z punkntem 2.
 * // czyli podswietlic zakladke "grupy" i dodać nazwe grupy do tablicy grupy groups.add(group_name)
 *
 *
 *
 *
 *
 * PS. to oczywiscie jeszcze nie wszystkie komunikaty ktore beda
 * PS2 tak jak mówilismy jakiś tydzień temu na discordzie, prawodopodobnie będziesz musiał zrobić jakiś wątek GUI, który czyta te powiadomienia i w zależności od danego powiadomienia wykona odpowiednią akcję
 * PS3 pewnie będziemy musieli zrobić jakiś semafor, żeby ten wątek nie czytał z pustej tablicy powiadomień
 *
 *
 * */