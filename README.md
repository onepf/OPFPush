OpenPush
========
The project is under development.
Releases will be announced later.

Steps to integrate
------------------

1. Create a configuration object and pass a listener to it    
    ````java
    OpenPushProviderConfig config = new OpenPushProviderConfig(this, new PushListener());
    ````
    
    The following methods of the listener need to be overriden
    ````java
    
    public void onMessage(Context context, Bundle extras, String providerName) {
    }

    public void onError(Context context, String message, String providerName) {
    }

    public void onRegistered(Context context, String token, String providerName) {
    }

    public void onUnregistered(Context context, String token, String providerName) {
    }
    ````

2. Pass the configuration object to OpenPushProvider 
    ````java
    OpenPushProvider.getInstance().init(config);
    ````
