This application is an example of the OPFPush library usage. It aims to show the basic steps of how a developer may use 
it for push messages in their own applications.

## How it works

**State**
After starting the application OPFPush tries to register on a push provider. After successful registration you get the registration id.
Then the registration id, the name of push provider and uuid are sent to the OPFPush 3rd-party server. 
UUID is used by the server for mapping concrete device and its registration id. So the application doesn't use the registration id to send messages.

Also you can unregister. After successful unregistration your uuid and registration id are removed from OPFPush server and you can't send messages to this uuid anymore.

**Contact list**
Then you can share your uuid with another device and add it to the contact list. After that you can send messages to all uuids from your contact list.
If you receive a message from unknown uuid you can copy it by long click on the message item and save it to the contact list. 

**Messages**
Messages are sent to the server with a list of receivers (uuids from your contact list). 
The OPFPush server use concrete push service (GCM, ADM or Nokia notifications) for each uuid. 
After that receivers get the message as a push message via OPFPush listener.

*Delivering of messages usually takes some time because push providers doesn't deliver push messages immediately.* 
 
**Send message via web page**
Also you can use [OPFPush web page][https://onepf-opfpush.appspot.com] to send messages to your devices.