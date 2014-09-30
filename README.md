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
            manifestPlaceholders = [packageId: "\${applicationId}".toString()]
            ...
        }
    }
    ````