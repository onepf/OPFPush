# OPFPush

The project is under development.
Releases will be announced later.

Currently OPFPush is a library that wraps Google Cloud Messaging, Nokia Notification Push,
Android Device Messaging and has possibility to integrate new push service.

For more information see [the website][9].



## Table Of Contents

- [Download](#user-content-download)
- [How To Use](#user-content-how-to-use)
- [Create Custom Push Provider](#user-content-create-custom-push-provider)
- [Implemented Push Services](#user-content-implemented-push-services)
    - [Google Cloud Messaging][4]
    - [Amazon Device Messaging][5]
    - [Nokia Notification][6]
- [License](#user-content-license)



## Download

Download [the latest AAR][10] or [the latest JAR][8]. Also you can grab it via Gradle
```groovy
compile 'org.onepf:opfpush:2.0'
```
for JAR dependency:
```groovy
compile 'org.onepf:opfpush:2.0@jar'
```

or Maven:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>2.0</version>
</dependency>
```
for JAR dependency:
```xml
<dependency>
    <groupId>org.onepf</groupId>
    <artifactId>opfpush</artifactId>
    <version>2.0</version>
    <type>jar</type>
</dependency>
```



## How To Use

Before setup `OPFPushHelper` you must setup your project files.
If you use [Android New Build System][7] and [AAR][11] dependencies:

0. Add to build.gradle file in your app module:

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

0. Add to AndroidManifest.xml file of your app:

      ```xml
      <uses-permission android:name="android.permission.INTERNET"/>
      <uses-permission android:name="android.permission.WAKE_LOCK"/>
      <uses-permission android:name="android.permission.BROADCAST_PACKAGE_REMOVED"/>
      ```

      and add for each used providers specific changes that you can find in provider README.md file.
      See section `Implemented Push Services`.

You can setup `OPFPushHelper` following steps:

1. Create `Options` object:

    ```java
    Options.Builder builder = new Options.Builder();
    builder.addProviders(new GCMProvider(this, GCM_SENDER_ID))
           .setRecoverProvider(true)
           .setSelectSystemPreferred(true)
           .setBackoff(new ExponentialBackoff());
    Options options = builder.build();
    ```

2. Init `OPFPushHelper` using created `Options` object:

    ```java
    OPFPushHelper.getInstance(this).init(options);
    ```

    Preferred place to init OPFPushHelper is the `Application` class of your app.

3. Add listener for `OPFPushHelper` events:

    ```java
    OPFPushHelper.getInstance(this).setListener(new OPFPushListener());
    ```

You can enable logging by call (by default it off):

```java
OPFPushLog.setLogEnable(true);
```

## Create Custom Push Provider

For create custom Push Provider you must create class that implement `PushProvider` interface.
Common functionality contains in `BasePushProvider` class, and we recommend subclass this class.

All provider has <i>Host Application</i>. Host application this is application that provider
push service work, such contains services, describe permission ant etc.
Usually this is store application, like Google Play Store for Google Cloud Messaging.

Requirements for custom Push Provider:

1. `register()` and `unregister()` method must execute asynchronously.
2. `isAvailable()` method must check device state for that provider has possibility to register,
    but this no mean that it registration can finish successfully.
3. `getRegistrationId()` method must return not null string only when it registered.
    In all other cases this method must return `null`.
4. `getName()` method must always return unique not null string for all using providers.
5. `getHostAppPackage()` method must return not null string with package of host application
    of push service.
6. `checkManifest()` method must check that all needed permissions, data and
   components described in manifests.
7. When `onRegistrationInvalid()` or `onUnavailable` method called
   you must reset all data about registration.

Provider notify `OPFPushHelper` about registration, or unregistration, or other events by
call methods in `ProviderCallback` class, such `onResult()` or `onMessage`.
You can get `ProviderCallback` object with call `OPFPushHelper.getProviderCallback()`.

For notify about registration or unregistration result you must call `ProviderCallback.onResult()`
with argument of `Result` class. `Result` class always contains provider name and optionally can
contains information about error(when failed) and token (when successfully).

For notify about receive new message call `ProviderCallback.onMessage()`.

Some provider can notify about deleted messages with call `ProviderCallback.onDeletedMessages()`.
Not all providers that can notify about this event can provide delete messages count.
For unknown count pass value `OPFPushHelper.MESSAGES_COUNT_UNKNOWN` as argument `messagesCount`.

## Implemented Push Services

1. [Google Cloud Messaging][1]. See [gcm-provider][4].
2. [Amazon Device Messaging][2]. See [adm-provider][5].
3. [Nokia Notification][3]. See [nokia-provider][6].



## License

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
[4]: ./providers/gcm
[5]: ./providers/adm
[6]: ./providers/nokia
[7]: http://tools.android.com/tech-docs/new-build-system
[8]: http://LINK_TO_the_latest_JAR.
[9]: http://www.onepf.org/openpush/
[10]: http://LINK_TO_the_latest_AAR.
[11]: http://tools.android.com/tech-docs/new-build-system/aar-format
