# MulitCom
Communicator made as academic project. Allows to send messages from Discord, Telegram and our own Client. All traffic goes through local server.

## General schema:
![Alt text](/doc/PROZ_graf.png)
## Communication between client and server:
```
public enum ClientToServerMessageType {
 REQUEST_LOGIN,
 REQUEST_REGISTER,
 LOGOUT,
 TEXT_TO_USER,
 ADD_USER_TO_FRIENDS,
 CONFIRMATION_OF_FRIENDSHIP,
 CREATE_GROUP,
 ADD_USER_TO_GROUP,
 TEXT_TO_GROUP,
 IMAGE,
}
```
```
public enum ServerToClientMessageType {
 CONFIRM_LOGIN,
 REJECT_LOGIN,
 LOGOUT,
 USER_WANTS_TO_BE_YOUR_FRIEND,
 USER_ACCEPTED_YOUR_FRIEND_REQUEST,
 TEXT_MESSAGE_FROM_USER,
 TEXT_MESSAGE_FROM_GROUP,
 USER_IS_NOT_CONNECTED,
 GROUP_NAME_OCCUPIED,
 USER_ADDED_YOU_TO_GROUP,
 IMAGE,
}
```
In order to add new bot the communication between this bot and the server must be based on those
messages.
## Server
Key classes and structures:
* Server – accepts all clients and runs ServerThread for each
* ServerThread – handles single client
* DatabaseHandler – interface between server and database. Gives answer whether user are
friends, user exists, password matches and much more.
* ConnectedUsers – ArrayList with all users that are online
* Groups – ArrayList with all groups, thanks to it we do not need to query a database each
time we are referring to groups. It will need to be changed if project will in bigger scale
## Client
Key classes:
* ClientPrinterThread – listener for client, passes all messages to NotificationHandler
* NotificationHandler – class to pass ServerToClientMessages to GUI
### Public functions:
```
void sendLoginOrRegisterRequest(String, String, ClientToServerMessageType)
boolean receiveLoginAnswer()
boolean addUserToFriends(String)
void confirmFriendship(String)
void sendTextMessageToUser(String, String)
void createGroup(String)
void sendTextMessageToGroup(String, String)
void addUserToGroup(String, String)
void logout()
```
Functions above are the interface between GUI and logic.
## Successful login schema:
![Alt text](/doc/PROZ_login.png)
## Successful text message to friend schema:
![Alt text](/doc/PROZ_mess.png)
Other actions are analogous to these two
## Bots
Bots are implemented with state machine, which has following states:
```
enum AvailableStates{
 INIT,
 CONNECTED_TO_CHAT,
 SELECT_LOGIN_OR_REGISTER,
 LOGIN_USERNAME,
 LOGIN_PASSWORD,
 REGISTER_USERNAME,
 REGISTER_PASSWORD,
 FRIEND_REQUEST_PENDING,
 IMAGE_SENDING,
 ADD_TO_FRIENDS,
 CREATE_GROUP,
 ADD_TO_GROUP,
 }
```
### Client advances through the state machine with following schema:
![Alt text](/doc/PROZ_bots.png)
New bots can be implemented in analogus manner. However programer must remeber to send
messages corresponding to the current state to the server.
## GUI
![Alt text](/doc/PROZ_gui.png)
GUI is made with 2 JFrames – Starting Screen and Main Window. After proper login
procedure Starting Screen disposes and Main Window pops up. Properly registered account is
immediately logged in.

Once Main Window is opened, GuiNotificationListener starts running and waits for
notifications from Client.NotificationHandler to show up.
Every notification is handled trough function in MainWindow, in order to add new
functionality GuiNotificationListener needs to be updated on new case scenario for this
function and MainWindow needs to have proper procedure pushing all the action forward.
For example: everytime user receives new message from user, function
getMessageFromUser() is selected in GuiNotificationListener with message from server and
all the information is passed by to ChatsTabs, which (ChatsTabs) is accesible from
MainWindow.

Each new functionality is expected to be added as a panel placed in a newly opened tab,
although there is some free space in both MainTab and NotificationPanel left to be filled with
JPanels.

Most of components (Panels) have reference to MainWindow JFrame, this allows user to i.a.
open and close chat tabs.
