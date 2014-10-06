OpenPush
========
The project is under development.
Releases will be announced later.

Currently OpenPush is a library that wraps Google Cloud Messaging, Nokia Notification Push,
Android Device Messaging and has possibility to integrate new push service.

For more information see [the website][9].



Download
--------

Download [the latest AAR][10] or [the latest JAR][8]. Also you can grab it via Gradle
```groovy
compile 'org.onepf:openpush:2.0'
```
for JAR dependency:
```groovy
compile 'org.onepf:openpush:2.0@jar'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>openpush</artifactId>
    <version>2.0</version>
</dependency>
```
for JAR dependency:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>openpush</artifactId>
    <version>2.0</version>
    <type>jar</type>
</dependency>
```



How To Use
----------

If you use [Android New Build System AAR][7] and AAR dependencies:

1. Add to build.gradle file in your app module:

       ```groovy
       android {
           defaultConfig {
               ...
               manifestPlaceholders = [packageId : "\${applicationId}".toString()]
               ...
           }
       }
       ```

If you use JAR dependencies:

1. Add to AndroidManifest.xml file of your app:

      ```xml
      <uses-permission android:name="android.permission.INTERNET"/>
      <uses-permission android:name="android.permission.WAKE_LOCK"/>
      <uses-permission android:name="android.permission.BROADCAST_PACKAGE_REMOVED"/>
      ```

      and add for each used providers specific changes that you can find in provider README.md file.
      See section `Implemented Push Services`.

Next steps common for both kind of dependencies.

2. Create `Options` object:

    ```java
    Options.Builder builder = new Options.Builder();
    builder.addProviders(new GCMProvider(this, GCM_SENDER_ID))
           .setRecoverProvider(true)
           .setSelectSystemPreferred(true)
           .setBackoff(new ExponentialBackoff(Integer.MAX_VALUE));
    Options options = builder.build();
    ```

3. Init `OpenPushHelper` using created `Options` object:

    ```java
    OpenPushHelper.getInstance(this).init(options);
    ```

    Preferred place to init OpenPushHelper is the `Application` class of your app.

4. Add listener for `OpenPushHelper` events:

    ```java
    OpenPushHelper.getInstance(this).setListener(new OpenPushListener());
    ```

You can enable logging by call (by default it off):

    ```java
    OpenPushLog.setLogEnable(true);
    ```



Implemented Push Services
-----------------------

1. [Google Cloud Messaging][1]. See [gcm-provider][4].
2. [Amazon Device Messaging][2]. See [adm-provider][5].
3. [Nokia Notification][3]. See [nokia-provider][6].



License
--------

    Copyright 2012-2014 One Platform Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


[1]: https://developer.android.com/google/gcm/index.html
[2]: https://developer.amazon.com/appsandservices/apis/engage/device-messaging
[3]: http://developer.nokia.com/resources/library/nokia-x/nokia-notifications.html
[4]: https://github.com/onepf/OpenPush/tree/readme/providers/gcm
[5]: https://github.com/onepf/OpenPush/tree/readme/providers/adm
[6]: https://github.com/onepf/OpenPush/tree/readme/providers/nokia
[7]: http://tools.android.com/tech-docs/new-build-system
[8]: LINK TO the latest JAR.
[9]: http://www.onepf.org/openpush/
[10]: LINK TO the latest AAR.
