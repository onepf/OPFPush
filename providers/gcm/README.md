# Google Cloud Messaging provider for OPFPush

[Google Cloud Messaging][1] implementation for Open Push.
See [guide how to porting GCM to OPFPUsh](../../README.md#user-content-porting-google-cloud-messaging-to-opfpush)


## Integrate in application

For work this module doe next things:
1. Add Google Play Services 4.0.30 or higher dependency to your project.

2. If you use JAR dependency add to AndroidManifest.xml of your application:

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
        <receiver android:name="org.onepf.openpush.BootCompleteReceiver">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED"/>
            </intent-filter>
        </receiver>

        <service
            android:name="org.onepf.openpush.gcm.GCMService"
            android:exported="false"/>
    </application>
    ````

For working this provider require Google Play Service. This is very large library with more than
22K methods, but we need only GCM source. For remove unused source you can add the
following code in `build.gradle` in your app:

````groovy
afterEvaluate { project ->
    android.applicationVariants.each { variant ->
        variant.javaCompile.dependsOn stripPlayServices
    }
}

task stripPlayServices << {
    def playServiceRootFolder = new File(rootProject.buildDir, "intermediates/exploded-aar/com.google.android.gms/play-services/")
    playServiceRootFolder.list().each { versionName ->
        def versionFolder = new File(playServiceRootFolder, versionName)
        copy {
            from (file(new File(versionFolder, "classes.jar")))
            into (file(versionFolder))
            rename { fileName ->
                fileName = "classes_orig.jar"
            }
        }
        tasks.create(name: "strip" + versionName, type: Jar) {
            destinationDir = versionFolder
            archiveName = "classes.jar"
            from (zipTree(new File(versionFolder, "classes_orig.jar"))) {
                exclude "com/google/ads/**"
                exclude "com/google/android/gms/analytics/**"
                exclude "com/google/android/gms/games/**"
                exclude "com/google/android/gms/maps/**"
                exclude "com/google/android/gms/panorama/**"
                exclude "com/google/android/gms/plus/**"
                exclude "com/google/android/gms/drive/**"
                exclude "com/google/android/gms/ads/**"
                exclude "com/google/android/gms/wallet/**"
                exclude "com/google/android/gms/wearable/**"
            }
        }.execute()
        delete {
            delete (file(new File(versionFolder, "classes_orig.jar")))
        }
    }
}
````

[1]: https://developer.android.com/google/gcm/index.html
