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

package support;

import android.os.AsyncTask;
import android.os.Build;

/**
 * Created by  Kirill Rozov on 18.09.14.
 */
public final class AsyncTaskCompat {
    public static <PARAM, PROGRESS, RESULT> AsyncTask<PARAM, PROGRESS, RESULT> execute(
            AsyncTask<PARAM, PROGRESS, RESULT> asyncTask, PARAM... params) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        } else {
            return asyncTask.execute(params);
        }
    }

    private AsyncTaskCompat() {
    }
}
