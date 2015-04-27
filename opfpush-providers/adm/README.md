Implementation of [Amazon Device Messaging][1] for OPFPush.

## Dependency

Amazon push provider depends on proprietary `amazon-device-messaging.jar` which can be found within Amazon Mobile App SDK.

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

Download [the latest AAR][adm-latest-aar] or grab via Gradle:
```groovy
compile 'org.onepf:opfpush-adm:0.2.1@aar'
```
    
or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush-adm</artifactId>
    <version>0.2.1</version>
    <type>aar</type>
</dependency>
```

You can also use JAR dependency.
Download [the latest JAR][adm-latest-jar] or grab via Gradle:
```groovy
compile 'org.onepf:opfpush-adm:0.2.1'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush-adm</artifactId>
    <version>0.2.1</version>
</dependency>
```

## How To Use

**AndroidManifest**

Add the following permissions to your AndroidManifest.xml file:

```xml
<permission android:name="${applicationId}.permission.RECEIVE_ADM_MESSAGE" />
<uses-permission
    android:name="${applicationId}.permission.RECEIVE_ADM_MESSAGE"
    android:protectionLevel="signature" />
```

also add the following receiver:

```xml
<receiver
    android:name="org.onepf.opfpush.adm.ADMReceiver"
    android:permission="com.amazon.device.messaging.permission.SEND">

    <intent-filter>
        <action android:name="com.amazon.device.messaging.intent.REGISTRATION" />
        <action android:name="com.amazon.device.messaging.intent.RECEIVE" />

        <category android:name="${applicationId}" />
    </intent-filter>
</receiver>
```

If you use JAR dependency, you also must add to your application AndroidManifest.xml file following:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:amazon="http://schemas.amazon.com/apk/res/android">

    <uses-permission android:name="com.amazon.device.messaging.permission.RECEIVE" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" />

    <application>

        <amazon:enable-feature
            android:name="com.amazon.device.messaging"
            android:required="false" />

        <service
            android:name="org.onepf.opfpush.adm.ADMService"
            android:exported="false" />

        <receiver android:name="org.onepf.opfpush.adm.LoginAccountsChangedReceiver">

            <intent-filter>
                <action android:name="android.accounts.LOGIN_ACCOUNTS_CHANGED" />
            </intent-filter>

        </receiver>

    </application>
</manifest>
```

**Proguard**

Add the following lines to your `proguard-project.txt` file:
```java
-dontwarn com.amazon.device.messaging.**
-keep class com.amazon.device.messaging.** {*;}
-keep public class * extends com.amazon.device.messaging.ADMMessageReceiver
-keep public class * extends com.amazon.device.messaging.ADMMessageHandlerBase
```

**Usage**

To use `ADMProvider` just add it to `Configuration` when building new instance, like this:

```java
Configuration.Builder builder = new Configuration.Builder();
builder.addProviders(new ADMProvider(context));
```

[1]: https://developer.amazon.com/appsandservices/apis/engage/device-messaging
[adm-latest-aar]: https://github.com/onepf/OPFPush/releases/download/v0.2.1/opfpush-adm-0.2.1.aar
[adm-latest-jar]: https://github.com/onepf/OPFPush/releases/download/v0.2.1/opfpush-adm-0.2.1.jar