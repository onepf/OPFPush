# OPFPush (alpha)

The project is under development.

Currently OPFPush is a library that wraps Google Cloud Messaging, Nokia Notification Push,
Amazon Device Messaging and has possibility to integrate new push service.


## Table Of Contents

- [Download](#user-content-download)
- [How To Use](#user-content-how-to-use)
- [Using of OPFPushReceiver](#user-content-using-of-opfpushreceiver)
- [Implemented Push Services](#user-content-implemented-push-services)
- [Create Custom Push Provider](#user-content-create-custom-push-provider)
- [Comparison of most popular push services](#user-content-comparison-of-most-popular-push-services)
- [License](#user-content-license)



## Download

Download [the latest AAR][opfpush-latest-aar] or [the latest JAR][opfpush-latest-jar]. Also you can grab it via Gradle.
For AAR dependency (you also must add OPFUtils dependency):
```groovy
compile 'org.onepf:opfpush:0.1.5@aar'
compile 'org.onepf:opfutils:0.1.15@aar'
```
for JAR dependency:
```groovy
compile 'org.onepf:opfpush:0.1.5'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>0.1.5</version>
    <type>aar</type>
</dependency>
```
for JAR dependency:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>0.1.5</version>
</dependency>
```


## How To Use

**Project files setup**

Before setup `OPFPush` you must setup your project files.

1. If you use JAR dependencies add to your AndroidManifest.xml file the following permissions:
    ```xml   
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
    ```
    
   and the following receivers:
    ```xml
    <receiver android:name="org.onepf.opfpush.BootCompleteReceiver">
        <intent-filter>
            <action android:name="android.intent.action.BOOT_COMPLETED"/>
        </intent-filter>
    </receiver>

    <receiver android:name="org.onepf.opfpush.PackageChangeReceiver">
        <intent-filter>
            <action android:name="android.intent.action.PACKAGE_REPLACED" />
            <action android:name="android.intent.action.PACKAGE_REMOVED" />

            <data android:scheme="package" />
        </intent-filter>
    </receiver>

    <receiver android:name="org.onepf.opfpush.RetryBroadcastReceiver"/>
    ```

2. Add for each used providers specific changes that you can find in provider README.md file. See section [Implemented Push Services](#user-content-implemented-push-services).

3. Add the following line to your `proguard-project.txt` file:
    ```
    -dontwarn edu.umd.cs.findbugs.annotations.*
    ```
   Also add the proguard rules specific for the each used provider that you can find in provider README.md file.
   See section [Implemented Push Services](#user-content-implemented-push-services).
   
**OPFPush setup**

You can setup `OPFPush` following steps:

1. Create `Configuration` object:

    ```java
    Configuration.Builder builder = new Configuration.Builder();
    builder.addProviders(new GCMProvider(this, GCM_SENDER_ID))
           .addProviders(
               new GCMProvider(this, GCM_SENDER_ID),
               new ADMProvider(this),
               new NokiaNotificationsProvider(this, NOKIA_SENDER_ID)
           )
           .setSelectSystemPreferred(true)
           .setEventListener(new PushEventListener(this));
    Configuration configuration = builder.build();
    ```

2. Init `OPFPush` using created `Configuration` object:

    ```java
    //Init OPFPush.
    OPFPush.init(this, configuration);
    ```

    Preferred place to init `OPFPush` is the `Application` class of your app.

3. Register `OPFPushHelper`:

    ```java
    //Start registration.
    OPFPush.getHelper().register();
    ```

4. You can enable logging by call (by default it off):
    ```java
    OPFLog.setEnabled(true);
    ```

##Using of OPFPushReceiver

You can use `BroadcastReceiver` instead of `EventListener` for receiving push events. 
Just extend `OPFPushReceiver` and add your receiver to AndroidManifest.xml with the following intent filter:

```xml
<receiver
    android:name="[YOUR_RECEIVER_NAME]"
    android:exported="false">
    
        <intent-filter>
            <action android:name="org.onepf.opfpush.intent.NO_AVAILABLE_PROVIDER" />
            <action android:name="org.onepf.opfpush.intent.RECEIVE" />
            <action android:name="org.onepf.opfpush.intent.REGISTRATION" />
            <action android:name="org.onepf.opfpush.intent.UNREGISTRATION" />
        </intent-filter>

</receiver>
```

**IMPORTANT** You can't use `EventListener` and `OPFPushReceiver` in the same project.


## Implemented Push Services

1. [Google Cloud Messaging][google-cloud-messaging]. See [GCM provider][opfpush-gcm].
2. [Amazon Device Messaging][amazon-device-messaging]. See [ADM provider][opfpush-adm].
3. [Nokia Notifications][nokia-notifications]. See [Nokia Notifications provider][opfpush-nokia].


## Create Custom Push Provider

For create custom Push Provider you must create class that implement `PushProvider` interface.
Common functionality contains in `BasePushProvider` class, and we recommend subclass this class.

All provider has <i>Host Application</i>. Host application this is application that provider
push service work, such contains services, describe permission ant etc.
Usually this is store application, like Google Play Store for Google Cloud Messaging.

Requirements for custom Push Provider:

1. `register()` and `unregister()` method must execute asynchronously.
2. `isAvailable()` method must check device state for that provider has possibility to register,
    but this no mean that it registration can finish successfully.
3. `isRegistered()` method must return true if provider is registered and false otherwise.
4. `getRegistrationId()` method must return not null string only when it registered.
    In all other cases this method must return `null`.
5. `getName()` method must always return unique not null string for all using providers.
6. `getHostAppPackage()` method must return not null string with package of host application
    of push service.
7. `checkManifest()` method must check that all needed permissions, data and
   components described in manifests.
8. When `onRegistrationInvalid()` or `onUnavailable` method called
   you must reset all data about registration.

Provider notify `OPFPushHelper` about registration, or unregistration, or other events by
call methods in `ReceivedMessageHandler` class, such `onRegistered()` or `onMessage()`.
You can get `ReceivedMessageHandler` object with call `OPFPush.getHelper().getReceivedMessageHandler()`.

For notify about successful registration or unregistration result you must call `ReceivedMessageHandler.onRegistered()` or `ReceivedMessageHandler.onUnregistered()`.

For notify about error of registration or unregistration you must call `ReceivedMessageHandler.onRegistrationError()` or `ReceivedMessageHandler.onUnregistrationError()`.
If you don't know what operation caused an error you must call `ReceivedMessageHandler.onError()`. `OPFPushHelper` chooses right callback method relying on current state in this case.

For notify about receive new message call `ReceivedMessageHandler.onMessage()`.

Some provider can notify about deleted messages with call `ReceivedMessageHandler.onDeletedMessages()`.
Not all providers that can notify about this event can provide delete messages count.
For unknown count pass value `OPFConstants.MESSAGES_COUNT_UNKNOWN` as argument `messagesCount`.


## Comparison of most popular push services

| Criteria                            | GCM   | ADM   | Nokia Notifications | OPFPush     |
| :---------------------------------- | :---: | :---: | :-----------------: | :---------: |
| Receive messages                    |   +   |   +   |          +          |      +      |
| Retry register on fail              |   -   |   +   |          +          |      +      |
| Retry register on fail after reboot |   -   |   -   |          -          |      +      |
| Retry unregister on fail            |   -   |   -   |          -          |      +      |
| Check is registration valid on boot |   -   |   -   |          -          |      +      |
| Retry register after updating app version |   -   |   -   |          -          |      +      |
| Retry register after updating android os |   -   |   -   |          -          |      +      |



## License

    Copyright 2012-2015 One Platform Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[google-cloud-messaging]: https://developer.android.com/google/gcm
[amazon-device-messaging]: https://developer.amazon.com/appsandservices/apis/engage/device-messaging
[nokia-notifications]: http://developer.nokia.com/resources/library/nokia-x/nokia-notifications
[opfpush-gcm]: ./opfpush-providers/gcm
[opfpush-adm]: ./opfpush-providers/adm
[opfpush-nokia]: ./opfpush-providers/nokia
[new-build-system]: http://tools.android.com/tech-docs/new-build-system
[opfpush-latest-jar]: https://github.com/onepf/OPFPush/releases/download/v0.1.5/opfpush-0.1.5.jar
[openpush-site]: http://www.onepf.org/openpush/
[opfpush-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.1.5/opfpush-0.1.5.aar
[aar-format-docs]: http://tools.android.com/tech-docs/new-build-system/aar-format
[BroadcastMessageListener.java]: ./samples/gcm_migrate_sample/src/main/java/org/onepf/opfpush/gcm_migrate_sample/BroadcastMessageListener.java
