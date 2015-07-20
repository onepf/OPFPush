# OPFPush

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-OPFPush-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1809)

OPFPush is an open source library which provides an easy way for developers to integrate android 
push notifications into their apps.

Currently OPFPush supports the following push providers: [Google Cloud Messaging][google-cloud-messaging],
[Nokia Notification Push][nokia-notifications], [Amazon Device Messaging][amazon-device-messaging] and
provides possibility to use the system push provider for a specific device.


## Table Of Contents
- [Download](#user-content-download)
- [How To Use](#user-content-how-to-use)
- [Using of OPFPushReceiver](#user-content-using-of-opfpushreceiver)
- [Notification payload support](#user-content-notification-payload-support)
- [Implemented Push Services](#user-content-implemented-push-services)
- [Create Custom Push Provider](#user-content-create-custom-push-provider)
- [Comparison of most popular push services](#user-content-comparison-of-most-popular-push-services)
- [Sample](#sample)
- [License](#user-content-license)



## Download

1. To use OPFPush you must add to your dependencies [OPFUtils library][opfutils].
   Download [the latest AAR][opfpush-latest-aar] of OPFPush and [the latest JAR][opfutils-latest-jar] of OPFUtils
   or grab it via Gradle:
   ```groovy
   compile 'org.onepf:opfpush:0.3.0@aar'
   compile 'org.onepf:opfutils:0.1.23'
   ```

   or Maven:
   ```xml
   <dependency>
      <groupId>org.onepf</groupId>
      <artifactId>opfpush</artifactId>
      <version>0.3.0</version>
      <type>aar</type>
   </dependency>
   <dependency>
       <groupId>org.onepf</groupId>
       <artifactId>opfutils</artifactId>
       <version>0.1.23</version>
   </dependency>
   ```

   You also can use JAR dependency. See [the following section][jar-dependency-using].

2. Add specific dependencies for each used push provider that you can find in the 
   section [Implemented Push Services](#user-content-implemented-push-services).

## How To Use

**Project files setup**

Add specific permissions and receivers to the AndroidManifest.xml file for each used push provider.
Add the proguard rules specific for the each used push provider.
You can find more information in the README.md files of implemented providers. 
See the section [Implemented Push Services](#user-content-implemented-push-services).
   
**OPFPush setup**

To setup `OPFPush` add the following piece of code to your `Application.onCreate()` method:
```java
//Enable OPFLogs:
OPFLog.setEnabled(BuildConfig.DEBUG, true); //debug logs will be enabled only in debug build.
//Create Configuration object:
Configuration.Builder builder = new Configuration.Builder();
//Add push providers that you want to use.
builder.addProviders( 
   new GCMProvider(this, GCM_SENDER_ID),
   new ADMProvider(this),
   new NokiaNotificationsProvider(this, NOKIA_SENDER_ID)
)
.setSelectSystemPreferred(true) //Select the system push provider for a specific device. (false by default).
.setEventListener(new PushEventListener(this)); //An implementation of EventListener interface.
Configuration configuration = builder.build();
//Init OPFPush using the created Configuration object:
OPFPush.init(this, configuration);
//Start registration.
OPFPush.getHelper().register();
```

You'll get the registration id into `EventListener.onRegistered()` callback.
After registration you'll start receiving push messages into `EventListener.onMessage()` callback.

If you want to start using GCM in your application but don't know how. See [the easiest way][easiest-gcm]
to implement GCM using OPFPush library.

##Using of OPFPushReceiver

You can use `BroadcastReceiver` instead of `EventListener` for receiving push events. 
See [the following section][opfpush-receiver-section]

##Notification payload support

[GCM Notification payload support][gcm-notification-payload-support] was added to the library.
Also we have implemented a similar mechanism for all supported push providers.
See [the following section][opf-notification-payload-support]

## Implemented Push Services

1. [Google Cloud Messaging][google-cloud-messaging].
    Download [the latest AAR][gcm-latest-aar] or grab via Gradle:
    ```groovy
    compile 'org.onepf:opfpush-gcm:0.3.0@aar'
    ```
    
    or Maven:
    ```xml
    <dependency>
        <groupId>org.onepf</groupId>
        <artifactId>opfpush-gcm</artifactId>
        <version>0.3.0</version>
        <type>aar</type>
    </dependency>
    ```
    
    See [GCM provider][opfpush-gcm] for more information .
    
2. [Amazon Device Messaging][amazon-device-messaging].
    Download [the latest AAR][adm-latest-aar] or grab via Gradle:
    ```groovy
    compile 'org.onepf:opfpush-adm:0.3.0@aar'
    ```
    
    or Maven:
    ```xml
    <dependency>
        <groupId>org.onepf</groupId>
        <artifactId>opfpush-adm</artifactId>
        <version>0.3.0</version>
        <type>aar</type>
    </dependency>
    ```
    
    See [ADM provider][opfpush-adm] for more information.
    
3. [Nokia Notifications][nokia-notifications].
    Download [the latest AAR][nokia-latest-aar] or grab via Gradle:
    ```groovy
    compile 'org.onepf:opfpush-nokia:0.3.0@aar'
    ```
        
    or Maven:
    ```xml
    <dependency>
        <groupId>org.onepf</groupId>
        <artifactId>opfpush-nokia</artifactId>
        <version>0.3.0</version>
        <type>aar</type>
    </dependency>
    ```

    See [Nokia Notifications provider][opfpush-nokia] for more information.

## Create Custom Push Provider

To create a custom push provider see [the following section][custom-push-provider].

## Comparison of most popular push services

| Criteria                            | GCM   | ADM   | Nokia Notifications | OPFPush     |
| :---------------------------------- | :---: | :---: | :-----------------: | :---------: |
| Receive messages                    |   +   |   +   |          +          |      +      |
| Multiple senders                    |   +   |   -   |          +          |      +      |
| Notification payload support        |   +   |   -   |          -          |      -      |
| Asynchronous registration and unregistration |   -   |   +   |          +          |      +      |
| Retry register on fail              |   -   |   +   |          +          |      +      |
| Retry register on fail after reboot |   -   |   -   |          -          |      +      |
| Retry unregister on fail            |   -   |   -   |          -          |      +      |
| Check is registration valid on boot |   -   |   -   |          -          |      +      |
| Retry register after updating app version |   -   |   -   |          -          |      +      |
| Retry register after changing [ANDROID_ID][android-id] |   -   |   -   |          -          |      +      |


## Sample

Take a look at the usage of the OPFPush library in our [sample application][sample].

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
[opfutils]: https://github.com/onepf/OPFUtils
[opfutils-latest-jar]: https://github.com/onepf/OPFUtils/releases/download/v0.1.23/opfutils-0.1.23.jar
[jar-dependency-using]: https://github.com/onepf/OPFPush/wiki/Using-of-JAR-dependencies
[opfpush-receiver-section]: https://github.com/onepf/OPFPush/wiki/Using-of-OPFPushReceiver
[custom-push-provider]: https://github.com/onepf/OPFPush/wiki/Create-custom-push-provider
[android-id]: http://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID
[opfpush-gcm]: ./opfpush-providers/gcm
[opfpush-adm]: ./opfpush-providers/adm
[opfpush-nokia]: ./opfpush-providers/nokia
[gcm-notification-payload-support]: https://developers.google.com/cloud-messaging/server-ref#notification-payload-support
[opf-notification-payload-support]: https://github.com/onepf/OPFPush/wiki/Notification-payload-support
[opfpush-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.0/opfpush-0.3.0.aar
[gcm-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.0/opfpush-gcm-0.3.0.aar
[adm-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.0/opfpush-adm-0.3.0.aar
[nokia-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.0/opfpush-nokia-0.3.0.aar
[easiest-gcm]: https://github.com/onepf/OPFPush/wiki/The-easiest-way-to-implement-GCM
[sample]: https://github.com/onepf/OPFPush/tree/master/samples/pushchat
