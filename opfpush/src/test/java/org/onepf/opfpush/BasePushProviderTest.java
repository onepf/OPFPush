package org.onepf.opfpush;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onepf.opfpush.mock.MockNamePushProvider;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;

/**
 * @author antonpp
 * @since 14.04.15
 */
@Config(sdk = JELLY_BEAN_MR2, manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
public class BasePushProviderTest {

    private BasePushProvider basePushProvider;

    @Before
    public void setUp() {
        basePushProvider = new MockNamePushProvider("Courier Push");
    }

    @Test
    public void testGetHostAppPackage() {
        Assert.assertEquals(MockNamePushProvider.DEFAULT_HOST_APP_PACKAGE, basePushProvider.getHostAppPackage());
    }

    @Test
    public void testGetContext() {
        Assert.assertEquals(RuntimeEnvironment.application.getApplicationContext(), basePushProvider.getContext());
    }
}
