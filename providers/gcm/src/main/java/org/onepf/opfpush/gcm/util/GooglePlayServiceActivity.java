package org.onepf.opfpush.gcm.util;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * @author Kirill Rozov
 * @since 02.10.14.
 */
public abstract class GooglePlayServiceActivity extends FragmentActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    // Request code to use when launching the resolution activity
    protected static final int REQUEST_RESOLVE_ERROR = 1001;
    // Bool to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mClient = createApiClient();
        mClient.registerConnectionFailedListener(this);
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onErrorDialogDismissed() {
        mResolvingError = false;
    }

    @NonNull
    public abstract GoogleApiClient createApiClient();

    public GoogleApiClient getClient() {
        return mClient;
    }

    // The rest of this code is all about building the error dialog

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment.newInstance(errorCode)
                .show(getSupportFragmentManager(), "errordialog");
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mResolvingError) {  // more about this later
            mClient.connect();
        }
    }

    @Override
    protected void onStop() {
        mClient.disconnect();
        super.onStop();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!mResolvingError) {
            if (result.hasResolution()) {
                try {
                    mResolvingError = true;
                    result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
                } catch (IntentSender.SendIntentException e) {
                    // There was an error with the resolution intent. Try again.
                    mClient.connect();
                }
            } else {
                // Show dialog using GooglePlayServicesUtil.getErrorDialog()
                showErrorDialog(result.getErrorCode());
                mResolvingError = true;
            }
        }
    }

    public static class ErrorDialogFragment extends DialogFragment {
        private static final String ARG_ERROR_CODE = "error_code";

        static ErrorDialogFragment newInstance(int errorCode) {
            Bundle args = new Bundle();
            args.putInt(ARG_ERROR_CODE, errorCode);
            ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
            dialogFragment.setArguments(args);
            return dialogFragment;
        }

        public ErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = getArguments().getInt(ARG_ERROR_CODE);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((GooglePlayServiceActivity) getActivity()).onErrorDialogDismissed();
        }
    }
}
