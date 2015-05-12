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

package org.onepf.opfpush.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.onepf.opfpush.listener.CheckManifestHandler;
import org.onepf.opfutils.OPFChecks;

import java.util.List;
import java.util.Locale;

/**
 * @author Roman Savin
 * @since 22.04.2015
 */
public final class CheckUtils {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private CheckUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Checks is a service has been described in the AndroidManifest.xml file.
     *
     * @param context The instance of {@link android.content.Context}.
     * @param service The checked service.
     */
    @SuppressWarnings("PMD.PreserveStackTrace")
    public static void checkService(@NonNull final Context context,
                                    @NonNull final ComponentName service,
                                    @Nullable final CheckManifestHandler checkManifestHandler) {
        final PackageManager packageManager = context.getPackageManager();
        try {
            packageManager.getServiceInfo(service, 0);
        } catch (PackageManager.NameNotFoundException e) {
            final String message = "Service " + service.getClassName()
                    + " hasn't been declared in AndroidManifest.xml";

            if (checkManifestHandler == null) {
                throw new IllegalStateException(message);
            } else {
                checkManifestHandler.onCheckManifestError(message);
            }
        }
    }

    public static void checkPermission(@NonNull final Context context,
                                       @NonNull final String permission,
                                       @Nullable final CheckManifestHandler checkManifestHandler) {

        checkPermission(
                context,
                permission,
                String.format(
                        Locale.US,
                        "You must add %s permission to the AndroidManifest.xml",
                        permission
                ),
                checkManifestHandler
        );
    }

    /**
     * Checks is a permission has been described in the AndroidManifest.xml file.
     *
     * @param context          The instance of {@link android.content.Context}.
     * @param permission       The checked permission.
     * @param exceptionMessage The exception message.
     */
    @SuppressWarnings("PMD.EmptyCatchBlock")
    public static void checkPermission(@NonNull final Context context,
                                       @NonNull final String permission,
                                       @NonNull final String exceptionMessage,
                                       @Nullable final CheckManifestHandler checkManifestHandler) {
        if (TextUtils.isEmpty(permission)) {
            throw new IllegalArgumentException("Permission can't be null or empty.");
        }

        try {
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            final String[] requestedPermissions = info.requestedPermissions;
            if (requestedPermissions != null) {
                for (String requestedPermission : requestedPermissions) {
                    if (TextUtils.equals(permission, requestedPermission)) {
                        return;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException ignore) {
            // ignore
        }

        if (checkManifestHandler == null) {
            throw new SecurityException(exceptionMessage);
        } else {
            checkManifestHandler.onCheckManifestError(exceptionMessage);
        }
    }

    public static void checkReceiver(@NonNull final Context context,
                                     @NonNull final Intent broadcastIntent,
                                     @Nullable final CheckManifestHandler checkManifestHandler) {
        checkReceiver(context, null, broadcastIntent, null, checkManifestHandler);
    }

    public static void checkReceiver(@NonNull final Context context,
                                     @NonNull final String receiverName,
                                     @NonNull final Intent broadcastIntent,
                                     @Nullable final CheckManifestHandler checkManifestHandler) {
        checkReceiver(context, receiverName, broadcastIntent, null, checkManifestHandler);
    }

    public static void checkReceiver(@NonNull final Context context,
                                     @NonNull final Intent broadcastIntent,
                                     @NonNull final String permission,
                                     @Nullable final CheckManifestHandler checkManifestHandler) {
        checkReceiver(context, null, broadcastIntent, permission, checkManifestHandler);
    }

    /**
     * Checks is a receiver has been described in the AndroidManifest.xml file.
     * <p/>
     * Uses the following steps for the check:
     * <ol>
     * <li>Checks are there receivers that can handle the {@code broadcastIntent}.</li>
     * <li>Checks is there receiver with the package name corresponding to the app package name.</li>
     * <li>If the {@code receiverName} is no {@code null},
     * checks is there receiver belong the founded receivers with the required name.</li>
     * <li>If the {code permission} is no {@code null}, checks does the founded receiver have this permission.</li>
     * </ol>
     *
     * @param context         The instance of {@link android.content.Context}.
     * @param receiverName    The name of the checked receiver.
     * @param broadcastIntent The intent which must be handled by the checked receiver.
     * @param permission      The permission that must be defined for the checked receiver.
     */
    public static void checkReceiver(@NonNull final Context context,
                                     @Nullable final String receiverName,
                                     @NonNull final Intent broadcastIntent,
                                     @Nullable final String permission,
                                     @Nullable final CheckManifestHandler checkManifestHandler) {
        if (checkManifestHandler == null) {
            OPFChecks.checkReceiver(context, receiverName, broadcastIntent, permission);
        } else {
            safeCheckReceiver(context, receiverName, broadcastIntent, permission, checkManifestHandler);
        }
    }

    private static void safeCheckReceiver(@NonNull final Context context,
                                          @Nullable final String receiverName,
                                          @NonNull final Intent broadcastIntent,
                                          @Nullable final String permission,
                                          @NonNull final CheckManifestHandler checkManifestHandler) {
        try {
            OPFChecks.checkReceiver(context, receiverName, broadcastIntent, permission);
        } catch (IllegalStateException e) {
            final String message = prepareCheckReceiverReport(context, e.getMessage(), receiverName, broadcastIntent);
            checkManifestHandler.onCheckManifestError(message);
        }
    }

    @SuppressWarnings({"PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.ConsecutiveLiteralAppends"})
    @NonNull
    private static String prepareCheckReceiverReport(@NonNull final Context context,
                                                     @Nullable final String exceptionMessage,
                                                     @Nullable final String receiverName,
                                                     @NonNull final Intent broadcastIntent) {
        final String packageName = context.getPackageName();

        final StringBuilder reportBuilder = new StringBuilder("checkReceiver error")
                .append(LINE_SEPARATOR).append("Exception message : ").append(exceptionMessage)
                .append(LINE_SEPARATOR).append("Expected receiverName : ").append(receiverName)
                .append(LINE_SEPARATOR).append("Intent action : ").append(broadcastIntent.getAction());

        final PackageManager packageManager = context.getPackageManager();
        final List<ResolveInfo> receivers = packageManager
                .queryBroadcastReceivers(broadcastIntent, PackageManager.GET_INTENT_FILTERS);
        if (receivers == null) {
            reportBuilder.append(LINE_SEPARATOR).append("queryBroadcastReceivers returns null");
        } else if (receivers.isEmpty()) {
            reportBuilder.append(LINE_SEPARATOR).append("queryBroadcastReceivers returns empty list");
        } else {
            reportBuilder.append(LINE_SEPARATOR)
                    .append("queryBroadcastReceivers returns not empty list. List size : ").append(receivers.size())
                    .append(LINE_SEPARATOR)
                    .append("PackageName : ").append(packageName);

            int emptyPackageNameCounter = 0;
            int emptyReceiverNameCounter = 0;
            for (ResolveInfo receiver : receivers) {
                if (TextUtils.isEmpty(receiver.activityInfo.packageName)) {
                    ++emptyPackageNameCounter;
                } else if (receiver.activityInfo.packageName.equals(packageName)) {
                    reportBuilder.append(LINE_SEPARATOR)
                            .append("Receiver with right package : ")
                            .append(receiver.activityInfo.name);
                }
                if (TextUtils.isEmpty(receiver.activityInfo.name)) {
                    ++emptyReceiverNameCounter;
                } else if (receiver.activityInfo.name.equals(receiverName)) {
                    reportBuilder.append(LINE_SEPARATOR)
                            .append("Receiver with right name has package : ")
                            .append(receiver.activityInfo.packageName);
                }
            }

            reportBuilder.append(LINE_SEPARATOR)
                    .append("Receivers with empty package name : ").append(emptyPackageNameCounter)
                    .append(LINE_SEPARATOR)
                    .append("Receivers with empty name : ").append(emptyReceiverNameCounter);
        }

        reportBuilder.append(LINE_SEPARATOR)
                .append("Available receivers:");

        PackageInfo receiversInfo;
        try {
            receiversInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_RECEIVERS);
        } catch (PackageManager.NameNotFoundException e) {
            reportBuilder.append("Could not get receivers for package ").append(packageName);
            return reportBuilder.toString();
        }
        ActivityInfo[] packageReceivers = receiversInfo.receivers;

        for (ActivityInfo packageReceiver : packageReceivers) {
            reportBuilder.append(LINE_SEPARATOR)
                    .append("Receiver package : ")
                    .append(packageReceiver.packageName)
                    .append(" receiver name : ")
                    .append(packageReceiver.name);
        }

        return reportBuilder.toString();
    }
}
