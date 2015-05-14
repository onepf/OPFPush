[Nokia Notifications][Nokia Notifications Page] implementation for OPFPush.

## Dependency

Nokia push provider depends on proprietary `nokia-push.jar` which can be found within Nokia Mobile App SDK.

You can download it yourself and manually add it to your project, or user our GitHub hosted repo instead:
```groovy
allprojects {
  repositories {
    ...
    // third-party dependencies
    maven { url 'https://raw.githubusercontent.com/onepf/OPF-mvn-repo/master/' }
  }
}
```

## Download

Download [the latest AAR][nokia-latest-aar] or grab via Gradle:
```groovy
compile 'org.onepf:opfpush-nokia:0.2.2@aar'
```
        
or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush-nokia</artifactId>
    <version>0.2.2</version>
    <type>aar</type>
</dependency>
```

You can also use JAR dependency.
Download [the latest JAR][nokia-latest-jar] or grab via Gradle:
```groovy
compile 'org.onepf:opfpush-nokia:0.2.2'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush-nokia</artifactId>
    <version>0.2.2</version>
</dependency>
```

## How To Use

**AndroidManifest**

Add the following permissions to your AndroidManifest.xml file:

```xml
<uses-permission android:name="${applicationId}.permission.C2D_MESSAGE" />
<permission
    android:name="${applicationId}.permission.C2D_MESSAGE"
    android:protectionLevel="signature" />
```

also add the following receiver:

```xml
<receiver
    android:name="org.onepf.opfpush.nokia.NokiaNotificationsReceiver"
    android:permission="com.nokia.pushnotifications.permission.SEND">
        <intent-filter>
            <!-- Receives the actual messages. -->
            <action android:name="com.nokia.pushnotifications.intent.RECEIVE" />
            <!-- Receives the registration id. -->
            <action android:name="com.nokia.pushnotifications.intent.REGISTRATION" />
            
            <category android:name="${applicationId}" />
        </intent-filter>
</receiver>
```

If you use JAR dependency, you also must add to your application AndroidManifest.xml file following:

```xml
<uses-permission android:name="com.nokia.pushnotifications.permission.RECEIVE"/>

<application>
    <service
        android:name="org.onepf.opfpush.nokia.NokiaNotificationService"
        android:exported="false"/>
</application>
```

**Usage**

To use `NokiaNotificationsProvider` just add it to `Configuration` when building new instance, like this:

```java
Configuration.Builder builder = new Configuration.Builder();
builder.addProviders(new NokiaNotificationsProvider(context, NOKIA_NOTIFICATION_SENDER_ID));
```

[Nokia Notifications Page]: http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html
[nokia-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.2.2/opfpush-nokia-0.2.2.aar
[nokia-latest-jar]: https://github.com/onepf/OPFPush/releases/download/v0.2.2/opfpush-nokia-0.2.2.jar
