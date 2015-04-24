package org.onepf.opfpush;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.configuration.Configuration;
import org.onepf.opfpush.listener.SimpleEventListener;
import org.onepf.opfpush.mock.MockPushProvider;
import org.onepf.opfpush.model.AvailabilityResult;
import org.onepf.opfpush.model.UnrecoverablePushError;
import org.onepf.opfpush.pushprovider.PushProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * @author antonpp
 * @since 14.04.15
 */
@Config(emulateSdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class OPFPushHelperTest extends Assert {

    private static final String TAG = OPFPushHelperTest.class.getSimpleName();
    private static final int BUFFER_INITIAL_CAPACITY = 80;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @After
    public void eraseSettingsInstance() {
        Field instanceField;
        synchronized (Settings.class) {
            try {
                instanceField = Settings.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Test
    public void testGetProviderName() {
        final String expected = "Courier";
        final PushProvider provider = new MockPushProvider.Builder()
                .setName(expected)
                .setAvailabilityResult(new AvailabilityResult(true))
                .build();
        final TestEventListener eventListener = new TestEventListener();
        final Configuration configuration = new Configuration.Builder()
                .addProviders(provider)
                .setEventListener(eventListener)
                .build();

//        OPFPush.init(RuntimeEnvironment.application, configuration);
        try {
            initOPFPush(configuration);
        } catch (IllegalAccessException | InstantiationException | NoSuchFieldException
                | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            Log.e(TAG, e.getMessage());
        }
        OPFPush.getHelper().register();

        assertTrue(eventListener.isRegistered());
        assertEquals(expected, OPFPush.getHelper().getProviderName());

        OPFPush.getHelper().unregister();
        assertFalse(eventListener.isRegistered());

        OPFPush.getHelper().register();

        assertTrue(eventListener.isRegistered());
        assertEquals(expected, OPFPush.getHelper().getProviderName());
    }

    private void initOPFPush(Configuration configuration) throws ClassNotFoundException
            , IllegalAccessException, InvocationTargetException, InstantiationException
            , NoSuchMethodException, NoSuchFieldException {
        final Class<?> helperClass = Class.forName("org.onepf.opfpush.OPFPushHelperImpl");
        final Constructor<?> constructor = helperClass.getDeclaredConstructors()[0];
        constructor.setAccessible(true);
        final Object helper = constructor.newInstance(RuntimeEnvironment.application);
        final Method helperInitMethod = helperClass.getDeclaredMethod("init", Configuration.class);
        helperInitMethod.invoke(helper, configuration);
        final Field helperField = OPFPush.class.getDeclaredField("helper");
        helperField.setAccessible(true);
        helperField.set(null, helper);
    }

    private final class TestEventListener extends SimpleEventListener {

        private boolean isRegistered;

        @Override
        public void onRegistered(@NonNull Context context, @NonNull String providerName, @NonNull String registrationId) {
            super.onRegistered(context, providerName, registrationId);

            isRegistered = true;
        }

        @Override
        public void onUnregistered(@NonNull Context context, @NonNull String providerName, @Nullable String registrationId) {
            super.onUnregistered(context, providerName, registrationId);

            isRegistered = false;
        }

        @Override
        public void onNoAvailableProvider(@NonNull Context context, @NonNull Map<String, UnrecoverablePushError> pushErrors) {
            StringBuilder stringBuilder = new StringBuilder(BUFFER_INITIAL_CAPACITY);
            stringBuilder.append("Push errors : ");
            for (Map.Entry entry : pushErrors.entrySet()) {
                stringBuilder.append("ProviderName : ")
                        .append(entry.getKey())
                        .append(" Error : ")
                        .append(entry.getValue())
                        .append('\n');
            }
            Log.e(TAG, stringBuilder.toString());
        }

        public boolean isRegistered() {
            return isRegistered;
        }
    }
}
