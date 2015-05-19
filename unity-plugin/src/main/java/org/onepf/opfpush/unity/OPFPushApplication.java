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

package org.onepf.opfpush.unity;

import android.app.Application;
import android.util.Log;
import org.onepf.opfutils.OPFLog;

import java.io.IOException;
import java.util.Scanner;

/**
 * @author Roman Savin
 * @since 15.05.2015
 */
public class OPFPushApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Scanner scanner;
        try {
            scanner = new Scanner(getAssets().open("configuration.txt"));

            if (scanner.hasNextLine()) {
                final String string = scanner.nextLine();
                Log.d("OPF", "Raw : " + string);
            } else {
                Log.d("OPF", "Raw : nothing");
            }
        } catch (IOException e) {
            OPFLog.e(e.getMessage());
        }
    }
}
