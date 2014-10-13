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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import org.onepf.opfpush.gcm.TestConstants;
import org.onepf.opfpush.gcm.R;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * @author Kirill Rozov
 * @since 10/9/14.
 */
@Implements(GooglePlayServicesUtil.class)
public class ShadowGooglePlayServiceUtil {

    @Implementation
    public static int isGooglePlayServicesAvailable(Context context) {
        try {
            PackageInfo packageInfo =
                    Robolectric.packageManager.getPackageInfo(TestConstants.GOOGLE_PLAY_SERVICES_PACKAGE, 0);
            if (packageInfo.versionCode >=
                    context.getResources().getInteger(R.integer.google_play_services_version)) {
                return ConnectionResult.SUCCESS;
            } else {
                return ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED;
            }
        } catch (PackageManager.NameNotFoundException e) {
            return ConnectionResult.SERVICE_MISSING;
        }
    }
}
