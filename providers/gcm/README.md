[Google Cloud Messaging][1] implementation for Open Push.

For work this module doe next things:
1. Add Google Play Services 3.1.59 or higher dependency to your project.

2. If you don't use AAR dependency add to your AndroidManifest.xml application:
    ````xml
    <uses-permission android:name="(your_application_package).permission.C2D_MESSAGE"/>
    <permission
        android:name="(your_application_package).permission.C2D_MESSAGE"
        android:protectionLevel="signature"/>

    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE"/>
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
    <uses-permission
        android:name="android.permission.GET_ACCOUNTS"
        android:maxSdkVersion="15"/>

    <application>
        <receiver
            android:name="org.onepf.openpush.gcm.GCMReceiver"
            android:permission="com.google.android.c2dm.permission.SEND">
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE"/>
                <category android:name="(your_application_package)"/>
            </intent-filter>
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.REGISTRATION"/>
                <category android:name="(your_application_package)"/>
            </intent-filter>
            <intent-filter>
                <action android:name="org.onepf.openpush.gcm.intent.UNREGISTRATION"/>
                <action android:name="org.onepf.openpush.gcm.intent.REGISTRATION"/>
            </intent-filter>
        </receiver>
        <receiver android:name="org.onepf.openpush.gcm.BootCompleteReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
            </intent-filter>
        </receiver>

        <service
            android:name="org.onepf.openpush.gcm.GCMService"
            android:exported="false"/>
    </application>
    ````

[1]: https://developer.android.com/google/gcm/index.html
