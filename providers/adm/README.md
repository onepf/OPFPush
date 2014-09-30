````xml
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
````