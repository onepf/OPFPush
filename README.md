OpenPush
========
    The project is under development.
    Releases will be announced later.

    Currently OpenPush is a library that wraps Google Cloud Messaging, Nokia Notification Push,
    Android Device Messaging and has possibility to integrate new push service.

Steps to integrate
------------------

1. Create a configuration object and pass a OpenPushHelper to it:

    ````java
    OpenPushHelper.getInstance().setListener(new OpenPushListener());
    Options.Builder builder = new Options.Builder();
    OpenPushHelper.getInstance().init(builder.create());
    ````

2. If you use AAR dependency no need to do anything, but for JAR dependencies you need do additional steps:

    Add to you application AndroidManifest.xml file:

    ````xml
    <uses-permission android:name="android.permission.INTERNET"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>
    <uses-permission android:name="android.permission.BROADCAST_PACKAGE_REMOVED"/>
    ````

    and add for each used providers specific changes that you can find in provider README.md.
   
3. If you use AAR dependency add to your project build.gradle file:

    ````groovy
    android {
        defaultConfig {
            ...
            manifestPlaceholders = [packageId : "\${applicationId}".toString()]
            ...
        }
    }
    ````

Supported Push Services
-----------------------

1. [Google Cloud Messaging][1]. See [gcm-provider][4].
2. [Amazon Device Messaging][2]. See [adm-provider][5].
3. [Nokia Notification][3]. See [nokia-provider][6].

[1]: https://developer.android.com/google/gcm/index.html
[2]: https://developer.amazon.com/appsandservices/apis/engage/device-messaging
[3]: http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html
[4]: https://github.com/onepf/OpenPush/tree/readme/providers/gcm
[5]: https://github.com/onepf/OpenPush/tree/readme/providers/adm
[6]: https://github.com/onepf/OpenPush/tree/readme/providers/nokia
