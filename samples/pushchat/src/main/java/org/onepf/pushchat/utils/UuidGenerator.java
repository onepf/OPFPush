/*
 * Copyright 2012-2015 One Platform Foundation
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

package org.onepf.pushchat.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import org.onepf.opfutils.OPFLog;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Roman Savin
 * @since 28.04.2015
 */
public final class UuidGenerator {

    private static final MessageDigest DIGESTER;

    static {
        MessageDigest tmpDigester = null;
        try {
            tmpDigester = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            OPFLog.e(e.getMessage());
        }
        DIGESTER = tmpDigester;
    }

    private UuidGenerator() {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public static String generateUuid(@NonNull final Context context) {
        final TelephonyManager telephonyManager;
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String IMEI = telephonyManager.getDeviceId();
        final String IMSI = telephonyManager.getSubscriberId();

        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        final WifiInfo wInfo = wifiManager.getConnectionInfo();
        final String macAddress = wInfo.getMacAddress();

        final String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        //noinspection StringBufferReplaceableByString
        final StringBuilder uuidBuilder = new StringBuilder()
                .append(IMEI == null ? "" : IMEI)
                .append(IMSI == null ? "" : IMSI)
                .append(macAddress == null ? "" : macAddress)
                .append(androidId);

        return crypt(uuidBuilder.toString());
    }

    @NonNull
    private static String crypt(@NonNull final String string) {
        if (DIGESTER == null || TextUtils.isEmpty(string)) {
            return string;
        }

        DIGESTER.update(string.getBytes());

        final byte[] hashBytes = DIGESTER.digest();
        final StringBuilder hexString = new StringBuilder();

        for (byte hashByte : hashBytes) {
            if ((0xff & hashByte) < 0x10) {
                hexString.append("0").append(Integer.toHexString((0xFF & hashByte)));
            } else {
                hexString.append(Integer.toHexString(0xFF & hashByte));
            }
        }
        return hexString.toString();
    }
}
