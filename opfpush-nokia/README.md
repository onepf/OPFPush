[Nokia Notifications][Nokia Notifications Page] implementation for OPFPush.

## How To Use

If you use JAR dependency, you must to add to your application AndroidManifest.xml file following:

```xml
<permission
       android:name="(your_application_package).permission.C2D_MESSAGE"
       android:protectionLevel="signature"/>

<uses-permission android:name="(your_application_package).permission.C2D_MESSAGE"/>
<uses-permission android:name="com.nokia.pushnotifications.permission.RECEIVE"/>

<application>
   <receiver
       android:name="org.onepf.openpush.nokia.NokiaNotificationReceiver"
       android:permission="com.nokia.pushnotifications.permission.SEND">
       <intent-filter>
           <action android:name="com.nokia.pushnotifications.intent.RECEIVE"/>
           <action android:name="com.nokia.pushnotifications.intent.REGISTRATION"/>
       </intent-filter>
   </receiver>

   <service
       android:name="org.onepf.openpush.nokia.NokiaNotificationService"
       android:exported="false"/>

</application>
```

If you use AAR dependency and NBS add the next code:

```groovy
android {
  defaultConfig {
    ...
    manifestPlaceholders = [packageId : "\${applicationId}".toString()]
    ...
  }
}
```

Tou use `NokiaNotificationsProvider` simple add it to `Options` when building new instance, like this:

```java
Options.Builder builder = new Options.Builder();
builder.addProviders(new NokiaNotificationsProvider(this, NOKIA_NOTIFICATION_SENDER_ID))
```

[Nokia Notifications Page]: http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html
