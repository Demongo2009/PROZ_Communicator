package Messages.serverToClient;

public enum ServerToClientMessageType {
    CONFIRM_LOGIN,
    REJECT_LOGIN,
    /**/
    USER_WANTS_TO_BE_YOUR_FRIEND,
    USER_ACCEPTED_YOUR_FRIEND_REQUEST,
    TEXT_MESSAGE_FROM_USER,
    USER_IS_NOT_CONNECTED,
    NO_GROUP_IN_DATABASE,
    IMAGE,

}
