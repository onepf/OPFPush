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
