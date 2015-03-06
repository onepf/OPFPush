/*
 * Copyright 2012-2014 One Platform Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onepf.opfpush.gcm.shadow;

import android.content.Context;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.onepf.opfpush.gcm.util.TestConstants;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * @author Kirill Rozov
 * @since 10/9/14.
 */
@Implements(GooglePlayServicesUtil.class)
public final class ShadowGooglePlayServiceUtil {

    @Implementation
    public static int isGooglePlayServicesAvailable(Context context) {
        try {
            Robolectric.packageManager.getPackageInfo(TestConstants.GOOGLE_PLAY_SERVICES_PACKAGE, 0);
            return ConnectionResult.SUCCESS;
        } catch (PackageManager.NameNotFoundException e) {
            return ConnectionResult.SERVICE_MISSING;
        }
    }
    
    private ShadowGooglePlayServiceUtil() {
        throw new UnsupportedOperationException();
    }
}
