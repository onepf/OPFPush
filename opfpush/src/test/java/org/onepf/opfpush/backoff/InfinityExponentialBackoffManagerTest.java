package org.onepf.opfpush.backoff;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.model.Operation;
import org.onepf.opfpush.testutil.Util;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.Field;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * @author antonpp
 * @since 14.04.15
 */
@Config(sdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class InfinityExponentialBackoffManagerTest extends Assert {

    private static final String TAG = InfinityExponentialBackoffManagerTest.class.getSimpleName();
    private static final String PROVIDER_A = "PROVIDER_A";
    private static final String PROVIDER_B = "PROVIDER_B";

    private static final int NUM_OF_TRIES = 7;

    private InfinityExponentialBackoffManager manager;

    @Before
    public void setUp() {
        manager = InfinityExponentialBackoffManager.getInstance();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @After
    public void eraseSettingsInstance() {
        synchronized (InfinityExponentialBackoffManager.class) {
            try {
                final Field instanceField = InfinityExponentialBackoffManager.class.getDeclaredField("instance");
                instanceField.setAccessible(true);
                instanceField.set(null, null);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

    @Test
    public void testGetInstance() {
        eraseSettingsInstance();
        manager = InfinityExponentialBackoffManager.getInstance();
        assertEquals(manager, InfinityExponentialBackoffManager.getInstance());
    }

    @Test
    public void testHasTries() {
        for (int i = 0; i < Util.NUM_TESTS; ++i) {
            for (final Operation operation : Operation.values()) {
                assertTrue(manager.hasTries(PROVIDER_A, operation));
                assertTrue(manager.hasTries(PROVIDER_B, operation));
                manager.getTryDelay(PROVIDER_A, operation);
                manager.getTryDelay(PROVIDER_B, operation);
            }
        }
    }

    private void getTryDelayRegister() {
        long prevDelay = manager.getTryDelay(PROVIDER_A, Operation.REGISTER);
        boolean isConstant = false;
        for (int i = 0; i < Util.NUM_TESTS; ++i) {
            final long delayA = manager.getTryDelay(PROVIDER_A, Operation.REGISTER);
            if (isConstant) {
                Assert.assertEquals(delayA, prevDelay);
            } else if (delayA != 2L * prevDelay) {
                isConstant = true;
                Assert.assertEquals(delayA, prevDelay);
            } else {
                Assert.assertEquals(delayA, 2L * prevDelay);
            }
            prevDelay = delayA;
            final long delayB = manager.getTryDelay(PROVIDER_B, Operation.REGISTER);
            if (isConstant) {
                Assert.assertEquals(delayB, prevDelay);
            } else if (delayB != 2L * prevDelay) {
                isConstant = true;
                Assert.assertEquals(delayB, prevDelay);
            } else {
                Assert.assertEquals(delayB, 2L * prevDelay);
            }
            prevDelay = delayB;
        }
    }

    private void getTryDelayUnregister() {
        long prevDelayA = manager.getTryDelay(PROVIDER_A, Operation.UNREGISTER);
        long prevDelayB = manager.getTryDelay(PROVIDER_B, Operation.UNREGISTER);
        boolean isConstantA = false;
        boolean isConstantB = false;
        for (int i = 0; i < Util.NUM_TESTS; ++i) {
            final long delayA = manager.getTryDelay(PROVIDER_A, Operation.UNREGISTER);
            final long delayB = manager.getTryDelay(PROVIDER_B, Operation.UNREGISTER);
            if (isConstantA) {
                Assert.assertEquals(delayA, prevDelayA);
            } else if (delayA != 2L * prevDelayA) {
                isConstantA = true;
                Assert.assertEquals(delayA, prevDelayA);
            } else {
                Assert.assertEquals(delayA, 2L * prevDelayA);
            }
            if (isConstantB) {
                Assert.assertEquals(delayB, prevDelayB);
            } else if (delayA != 2L * prevDelayB) {
                isConstantB = true;
                Assert.assertEquals(delayB, prevDelayB);
            } else {
                Assert.assertEquals(delayB, 2L * prevDelayB);
            }
            prevDelayA = delayA;
            prevDelayB = delayB;
        }
    }

    @Test
    public void testGetTryDelay() {
        for (final Operation operation : Operation.values()) {
            switch (operation) {
                case REGISTER:
                    getTryDelayRegister();
                    break;
                case UNREGISTER:
                    getTryDelayUnregister();
                    break;
            }
        }
    }

    @Test
    public void testReset() {
        for (final Operation operation : Operation.values()) {
            for (int i = 0; i < NUM_OF_TRIES; ++i) {
                manager.getTryDelay(PROVIDER_A, operation);
                manager.getTryDelay(PROVIDER_B, operation);
            }
            manager.reset(PROVIDER_A, operation);
            assertTrue(manager.getTryDelay(PROVIDER_A, operation) != manager.getTryDelay(PROVIDER_B, operation));
        }
    }
}
