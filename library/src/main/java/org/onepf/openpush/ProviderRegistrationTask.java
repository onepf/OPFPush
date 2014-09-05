package org.onepf.openpush;

import android.os.AsyncTask;

/**
 * Created by krozov on 05.09.14.
 */
class ProviderRegistrationTask extends AsyncTask<PushProvider, Void, Void> {
    @Override
    protected Void doInBackground(PushProvider... params) {
        params[0].register();
        return null;
    }
}
