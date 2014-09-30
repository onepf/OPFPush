````xml
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
````