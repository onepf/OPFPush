# OPFPush

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-OPFPush-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1809)

OPFPush is an open source library which provides an easy way for developers to integrate android 
push notifications into their apps.

Currently OPFPush supports the following push providers: [Google Cloud Messaging][google-cloud-messaging],
[Nokia Notification Push][nokia-notifications], [Amazon Device Messaging][amazon-device-messaging] and
provides possibility to use the system push provider for a specific device.

## How To Use

**Add dependencies**

The main dependencies are the `opfmaps` module and the [OPFUtils][opfutils] library:

```gradle
compile 'org.onepf:opfpush:0.3.1@aar'
compile 'org.onepf:opfutils:0.1.25'
```

Then you have to add at least one map provider dependency.

[GCM Provider][opfpush-gcm]:

```gradle
compile 'org.onepf:opfpush-gcm:0.3.1@aar'
compile 'com.google.android.gms:play-services:7.8.0'
```

[ADM Push Provider][opfpush-adm]:

```gradle
compile 'org.onepf:opfpush-adm:0.3.1@aar'
provided 'com.amazon:amazon-device-messaging:1.0.1'
```

[Nokia Push Provider][opfpush-nokia]:

```gradle
compile 'org.onepf:opfpush-nokia:0.3.1@aar'
compile 'com.nokia:push:1.0'
```

*NOTE:* If you use `ADMProvider` or/and `NokiaPushProvider` you have to add the following repo which hosts `amazon-device-messaging.jar` and `nokia-push.jar`:

```gradle
allprojects {
  repositories {
    ...
    // third-party dependencies
    maven { url 'https://raw.githubusercontent.com/onepf/OPF-mvn-repo/master/' }
  }
}
```

**Update AndroidManfest.xml**

Add permissions and receivers required for each used Push Provider:

```xml
<!--gcm permissions-->
<uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />
<permission
   android:name="${applicationId}.permission.C2D_MESSAGE"
   android:protectionLevel="signature" />
   
<!--adm permissions-->
<permission android:name="${applicationId}.permission.RECEIVE_ADM_MESSAGE" />
<uses-permission
   android:name="${applicationId}.permission.RECEIVE_ADM_MESSAGE"
   android:protectionLevel="signature" />
   
<!--nokia-->
<!--The same as for gcm-->
   
<application>

   <!--gcm receiver-->
   <receiver
      android:name="com.google.android.gms.gcm.GcmReceiver"
      android:exported="true"
      android:permission="com.google.android.c2dm.permission.SEND">
      <intent-filter>
         <action android:name="com.google.android.c2dm.intent.REGISTRATION" />
         <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
         <category android:name="${applicationId}"/>
      </intent-filter>
   </receiver>
   
   <!--adm receiver-->
   <receiver
      android:name="org.onepf.opfpush.adm.ADMReceiver"
      android:permission="com.amazon.device.messaging.permission.SEND">

      <intent-filter>
         <action android:name="com.amazon.device.messaging.intent.REGISTRATION" />
         <action android:name="com.amazon.device.messaging.intent.RECEIVE" />

         <category android:name="${applicationId}" />
      </intent-filter>
   </receiver>
   
   <!--nokia receiver-->
   <receiver
      android:name="org.onepf.opfpush.nokia.NokiaNotificationsReceiver"
      android:permission="com.nokia.pushnotifications.permission.SEND">
      
      <intent-filter>
         <action android:name="com.nokia.pushnotifications.intent.RECEIVE" />
         <action android:name="com.nokia.pushnotifications.intent.REGISTRATION" />

         <category android:name="${applicationId}" />
      </intent-filter>
   </receiver>

</application>
```

**Initialization**

To setup `OPFPush` add the following piece of code to your `Application.onCreate()` method:
```java
public class MyApplication extends Application {

   @Override
   public void onCreate() {
      super.onCreate();
      OPFLog.setEnabled(BuildConfig.DEBUG, true); //Optional. It enables debug logs of the OPFMaps library in the debug build of your apk.

      final Configuration configuration = new Configuration.Builder()
            .addProviders(new GCMProvider(this, GCM_SENDER_ID), new ADMProvider(this), new NokiaNotificationsProvider(this, NOKIA_SENDER_ID)) //Add all providers. The priority of the providers corresponds to the order in which they were added.
            .setSelectSystemPreferred(true) //If you set true, the system push provider will get the highest priority. Default value is false.
            .setEventListener(new PushEventListener(this)) //An implementation of EventListener interface.
            .build();
      
      OPFPush.init(this, configuration); //Init OPFPush using the created Configuration object:
      OPFPush.getHelper().register(); //Start registration.
   }
}
```

You'll get the registration id into `EventListener.onRegistered()` callback.
After registration you'll start receiving push messages into `EventListener.onMessage()` callback.

## Sample

Take a look at the usage of the OPFPush library in our [sample application][sample].

##More Information

**Simple interation of Google Cloud Messaging**

If you want to start using GCM in your application but don't know how. See [the easiest way][easiest-gcm]
to implement GCM using OPFPush library.

**Using of OPFPushReceiver**

You can use `BroadcastReceiver` instead of `EventListener` for receiving push events. 
See [the following section][opfpush-receiver-section]

**Using of JAR dependencies instead of AARs**

You also can use JAR dependencies. See [the following section][jar-dependency-using].

**Notification payload support**

[GCM Notification payload support][gcm-notification-payload-support] was added to the library.
Also we have implemented a similar mechanism for all supported push providers.
See [the following section][opf-notification-payload-support]

**Create Custom Push Provider**

To create a custom push provider see [the following section][custom-push-provider].

## Comparison of most popular push services

| Criteria                            | GCM   | ADM   | Nokia Notifications | OPFPush     |
| :---------------------------------- | :---: | :---: | :-----------------: | :---------: |
| Receive messages                    |   +   |   +   |          +          |      +      |
| Multiple senders                    |   +   |   -   |          +          |      +      |
| Notification payload support        |   +   |   -   |          -          |      +      |
| Asynchronous registration and unregistration |   -   |   +   |          +          |      +      |
| Retry register on fail              |   -   |   +   |          +          |      +      |
| Retry register on fail after reboot |   -   |   -   |          -          |      +      |
| Retry unregister on fail            |   -   |   -   |          -          |      +      |
| Check is registration valid on boot |   -   |   -   |          -          |      +      |
| Retry register after updating app version |   -   |   -   |          -          |      +      |
| Retry register after changing [ANDROID_ID][android-id] |   -   |   -   |          -          |      +      |

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
[opfutils-latest-jar]: https://github.com/onepf/OPFUtils/releases/download/v0.1.25/opfutils-0.1.25.jar
[jar-dependency-using]: https://github.com/onepf/OPFPush/wiki/Using-of-JAR-dependencies
[opfpush-receiver-section]: https://github.com/onepf/OPFPush/wiki/Using-of-OPFPushReceiver
[custom-push-provider]: https://github.com/onepf/OPFPush/wiki/Create-custom-push-provider
[android-id]: http://developer.android.com/reference/android/provider/Settings.Secure.html#ANDROID_ID
[opfpush-gcm]: ./opfpush-providers/gcm
[opfpush-adm]: ./opfpush-providers/adm
[opfpush-nokia]: ./opfpush-providers/nokia
[gcm-notification-payload-support]: https://developers.google.com/cloud-messaging/server-ref#notification-payload-support
[opf-notification-payload-support]: https://github.com/onepf/OPFPush/wiki/Notification-payload-support
[opfpush-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.1/opfpush-0.3.1.aar
[gcm-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.1/opfpush-gcm-0.3.1.aar
[adm-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.1/opfpush-adm-0.3.1.aar
[nokia-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.3.1/opfpush-nokia-0.3.1.aar
[easiest-gcm]: https://github.com/onepf/OPFPush/wiki/The-easiest-way-to-implement-GCM
[sample]: https://github.com/onepf/OPFPush/tree/master/samples/pushchat
