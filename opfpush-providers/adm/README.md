Implementation of [Amazon Device Messaging][1] for OPFPush.

## How To Use

If you use JAR dependency, you must to add to your application AndroidManifest.xml file following:

```xml
<manifest xmlns:amazon="http://schemas.amazon.com/apk/res/android">
    <uses-permission android:name="com.amazon.device.messaging.permission.RECEIVE"/>
    <permission
        android:name="(your_application_package).permission.RECEIVE_ADM_MESSAGE"
        android:protectionLevel="signature"/>

    <uses-permission android:name="(your_application_package).permission.RECEIVE_ADM_MESSAGE"/>

    <application>
        <amazon:enable-feature
            android:name="com.amazon.device.messaging"
            android:required="false"/>

        <service
            android:name="org.onepf.openpush.adm.ADMService"
            android:exported="false"/>

        <receiver
            android:name="org.onepf.openpush.adm.ADMReceiver"
            android:permission="com.amazon.device.messaging.permission.SEND">

            <intent-filter>
                <action android:name="com.amazon.device.messaging.intent.REGISTRATION"/>
                <action android:name="com.amazon.device.messaging.intent.RECEIVE"/>

                <category android:name="(your_application_package)"/>
            </intent-filter>
        </receiver>
    </application>
</manifest>
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

To use `ADMProvider` simple add it to `Options` when building new instance, like this:

```java
Options.Builder builder = new Options.Builder();
builder.addProviders(new ADMProvider(this))
```

[1]: https://developer.amazon.com/appsandservices/apis/engage/device-messaging
