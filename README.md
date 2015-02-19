# OPFPush

The project is under development.
Releases will be announced later.

Currently OPFPush is a library that wraps Google Cloud Messaging, Nokia Notification Push,
Android Device Messaging and has possibility to integrate new push service.

For more information see [the website][openpush-site].



## Table Of Contents

- [Download](#user-content-download)
- [How To Use](#user-content-how-to-use)
- [Create Custom Push Provider](#user-content-create-custom-push-provider)
- [Implemented Push Services](#user-content-implemented-push-services)
- [Porting Google Cloud Messaging to OPFPush](#user-content-porting-google-cloud-messaging-to-opfpush)
- [License](#user-content-license)
- [Comparison of most popular push services](#user-content-comparison-of-most-popular-push-services)



## Download

Download [the latest AAR][opfpush-latest-aar] or [the latest JAR][opfpush-latest-jar]. Also you can grab it via Gradle
```groovy
compile 'org.onepf:opfpush:2.0'
```
for JAR dependency:
```groovy
compile 'org.onepf:opfpush:2.0@jar'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>2.0</version>
</dependency>
```
for JAR dependency:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>2.0</version>
    <type>jar</type>
</dependency>
```


## How To Use

Before setup `OPFPushHelper` you must setup your project files.
If you use [Android New Build System][new-build-system] and [AAR][aar-format-docs] dependencies:

0. Add to build.gradle file in your app module:

    ```groovy
    android {
       defaultConfig {
           ...
           manifestPlaceholders = [packageId : "\${applicationId}".toString()]
           ...
       }
    }
    ```

If you use JAR dependencies:

0. Add to AndroidManifest.xml file of your app:

    ```xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.BROADCAST_PACKAGE_REMOVED"/>
    ```

    and add for each used providers specific changes that you can find in provider README.md file.
    See section `Implemented Push Services`.

You can setup `OPFPushHelper` following steps:

1. Create `Options` object:

    ```java
    Options.Builder builder = new Options.Builder();
    builder.addProviders(new GCMProvider(this, GCM_SENDER_ID))
           .setRecoverProvider(true)
           .setSelectSystemPreferred(true)
           .setBackoff(new ExponentialBackoff());
    Options options = builder.build();
    ```

2. Init `OPFPushHelper` using created `Options` object:

    ```java
    //Register messages listener.
    OPFPushHelper.getInstance(this).setMessageListener(new MessageListener());
    //Init OPFPushHelper provider.
    OPFPushHelper.getInstance(this).init(options);
    ```

    Preferred place to init OPFPushHelper is the `Application` class of your app.

4. Register `OPFPushHelper`:

    ```java
    //Register registration/unregistration listener.
    OPFPushHelper.getInstance(this).setListener(new OPFPushListener());
    //Start registration.
    OPFPushHelper.getInstance(this).register();
    ```

You can enable logging by call (by default it off):

```java
OPFLog.setEnabled(true);
```



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
3. `getRegistrationId()` method must return not null string only when it registered.
    In all other cases this method must return `null`.
4. `getName()` method must always return unique not null string for all using providers.
5. `getHostAppPackage()` method must return not null string with package of host application
    of push service.
6. `checkManifest()` method must check that all needed permissions, data and
   components described in manifests.
7. When `onRegistrationInvalid()` or `onUnavailable` method called
   you must reset all data about registration.

Provider notify `OPFPushHelper` about registration, or unregistration, or other events by
call methods in `ProviderCallback` class, such `onResult()` or `onMessage`.
You can get `ProviderCallback` object with call `OPFPushHelper.getProviderCallback()`.

For notify about registration or unregistration result you must call `ProviderCallback.onResult()`
with argument of `Result` class. `Result` class always contains provider name and optionally can
contains information about error(when failed) and token (when successfully).

For notify about receive new message call `ProviderCallback.onMessage()`.

Some provider can notify about deleted messages with call `ProviderCallback.onDeletedMessages()`.
Not all providers that can notify about this event can provide delete messages count.
For unknown count pass value `OPFPushHelper.MESSAGES_COUNT_UNKNOWN` as argument `messagesCount`.

If you wanna implement provider that can send message to server you must implement
`SenderPushProvider` interface that extends `PushProvider` interface. `SenderPushProvider` interface
contains method `send(Message)` that send message to server asynchronous.


## Porting Google Cloud Messaging to OPFPush

For porting Google Cloud Messaging (GCM) to OPFPush you need do the next steps:

1. Add initialization of OPFPush in `Application` class of you app:

    ````java
    Options.Builder builder = new Options.Builder();
    builder.addProviders(new GCMProvider(this, GCM_SENDER_ID));
    Options options = builder.build();
    ````

2. Set message events listener:

    ````java
    OPFPushHelper pushHelper = OPFPushHelper.getInstance(this);
    pushHelper.setMessageListener(new BroadcastMessageListener(this));
    pushHelper.init(options);
    ````

    [BroadcastMessageListener.java][BroadcastMessageListener.java] class redirect message events
    from OPFPush message events listener to existing BroadcastReceiver, that you use for GCM.
    You must add this class to your source for work with it.

3. Change intent filter for BroadcastReceiver, that you use for GCM from:

    ````xml
    <receiver
        android:name=".GcmBroadcastReceiver"
        android:permission="com.google.android.c2dm.permission.SEND" >
        <intent-filter>
            <action android:name="com.google.android.c2dm.intent.RECEIVE" />
            <category android:name="com.google.android.gcm.demo.app" />
        </intent-filter>
    </receiver>
    ````

    to

    ````xml
    <receiver android:name=".GcmBroadcastReceiver">
        <intent-filter>
            <action android:name="org.onepf.opfpush.intent.RECEIVE" />
        </intent-filter>
    </receiver>
    ````

4. Change registration in activity from:

    ````java
    if (checkPlayServices()) {
        gcm = GoogleCloudMessaging.getInstance(this);
        String regid = getRegistrationId(context);
        if (regid.isEmpty()) {
            registerInBackground();
        }
    }
    ````

    to:

    ````java
    mPushHelper = OPFPushHelper.getInstance(this);
    mPushHelper.setListener(new EventListener());
    if (checkPlayServices()) {
        if (!mPushHelper.isRegistered()) {
            mPushHelper.register();
        }
    }
    ````

5. Change get message type from intent in `onHandleIntent()` method of your service
   that handling GCM messages from:

    ````java
    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
    String messageType = gcm.getMessageType(intent);
    ````

    to

    ````java
    String messageType = BroadcastMessageListener.getMessageType(intent);
    ````

That's all. You don't need handle registration in `AsyncTask`, handle error and
can retry registration on fail - OPFPush already include this features for you.

You can see migrated from GCM sample code in [GCM Migrate Sample](./samples/gcm_migrate_sample).



## Implemented Push Services

1. [Google Cloud Messaging][google-cloud-messaging]. See [GCM provider][opfpush-gcm].
2. [Amazon Device Messaging][amazon-device-messaging]. See [ADM provider][opfpush-adm].
3. [Nokia Notifications][nokia-notifications]. See [Nokia Notifications provider][opfpush-nokia].



## Comparison of most popular push services

| Criteria                            | GCM   | ADM   | Nokia Notifications | OPFPush     |
| :---------------------------------- | :---: | :---: | :-----------------: | :---------: |
| Receive messages                    |   +   |   +   |          +          |      +      |
| Send messages                       |   +   |   -   |          -          |      +      |
| Retry register on fail              |   -   |   +   |          +          |      +      |
| Configurable retry mechanism        |   -   |   -   |        ON/OFF       |      +      |
| Retry register on fail after reboot |   -   |   -   |          -          |      +      |
| Retry unregister on fail            |   -   |   -   |          -          |      +      |
| Check is registration valid on boot |   -   |   -   |          -          |      +      |



## License

    Copyright 2012-2014 One Platform Foundation

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
[opfpush-gcm]: ./opfpush-gcm
[opfpush-adm]: ./opfpush-adm
[opfpush-nokia]: ./opfpush-nokia
[new-build-system]: http://tools.android.com/tech-docs/new-build-system
[opfpush-latest-jar]: http://LINK_TO_the_latest_JAR.
[openpush-site]: http://www.onepf.org/openpush/
[opfpush-latest-aar]: http://LINK_TO_the_latest_AAR.
[aar-format-docs]: http://tools.android.com/tech-docs/new-build-system/aar-format
[BroadcastMessageListener.java]: ./samples/gcm_migrate_sample/src/main/java/org/onepf/opfpush/gcm_migrate_sample/BroadcastMessageListener.java
