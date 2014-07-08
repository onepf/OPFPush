OpenPush
========
The project is under development.
Releases will be announced later.

Currently OpenPush is a library that wraps Google and Nokia push services.
We are planning to add support for several other push providers, too.
If you have an idea how we can impove our current library (add certain push support, change API and so on) - please share it with us.
You can create an issue in the corresponding section or create a pull request.

Steps to integrate
------------------

1. Create a configuration object and pass a listener to it    
    ````java
    OpenPushProviderConfig config = new OpenPushProviderConfig(this, new PushListener());
    ````
    
    The following methods of the listener need to be overriden
    ````java
    
    public void onMessage(Context context, Bundle extras, String providerName) {
    // process the message here
    }

    public void onError(Context context, String message, String providerName) {
    //process the error here
    }

    public void onRegistered(Context context, String token, String providerName) {
    // successfuly registered
    }

    public void onUnregistered(Context context, String token, String providerName) {
    //unregistered
    }
    ````

2. Pass the configuration object to OpenPushProvider 
    ````java
    OpenPushProvider.getInstance().init(config);
    ````

3. Change the AndroidManifest.xml   
    ````xml
    <!--Common for all providers-->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.GET_ACCOUNTS" /> 
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <!--Google -->
    <uses-permission android:name="your.package.name.permission.C2D_MESSAGE" />
    <uses-permission android:name="com.google.android.c2dm.permission.RECEIVE" />
   <permission android:name="your.package.name.permission.C2D_MESSAGE"
        android:protectionLevel="signature" />
    <uses-permission android:name="your.package.name.permission.C2D_MESSAGE" />

    <application ...>
        <receiver
            android:name="org.onepf.openpush.gcm.GcmBroadcastReceiver"
            android:permission="com.google.android.c2dm.permission.SEND" >
            <intent-filter>
                <action android:name="com.google.android.c2dm.intent.RECEIVE" />
                <category android:name="org.onepf.openpush.sample" />
            </intent-filter>
        </receiver>

        <service android:name="org.onepf.openpush.gcm.GcmIntentService"
            android:exported="false"/>
    </application>
    ````
   
