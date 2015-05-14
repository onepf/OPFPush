This application is an example of the OPFPush library usage. It aims to show the basic steps of how a developer may use 
it for push messages in their own applications.

## How it works

**State**

OPFPush tries to register on an available push provider. After successful registration you get the registration id.
Then the registration id, the name of the push provider and uuid are sent to the OPFPush 3rd-party server. 
UUID is used by the server for mapping specific device and its registration id. So the application doesn't use the registration id to send messages.
After registration you can share uuid and use it to send messages.

Also, you can unregister. After successful unregistration your uuid and registration id are removed from OPFPush server and you can't send messages to this uuid anymore.

**Contacts**

You can add uuids to the contact list. After that you can send messages to all uuids from your contact list.
If you receive a message from an unknown sender you can copy its uuid by long click on the message item and save it to the contact list. 

**Messages**

Messages are sent to the server with a list of receivers (uuids from your contact list). 
The OPFPush server uses specific push service (GCM, ADM or Nokia Notifications) for each uuid. 
After that receivers get the message as a push message via OPFPush listener.

*Delivering of messages usually takes some time because push providers don't deliver push messages immediately.* 
 
**Send message via web page**

You can also use [OPFPush web page][opfpush-server-link] to send messages to your devices.

[opfpush-server-link]: https://onepf-opfpush.appspot.com
