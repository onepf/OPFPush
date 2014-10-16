package org.onepf.opfpush.baidu;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.baidu.android.pushservice.PushManager;

import org.onepf.opfpush.BasePushProvider;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_API_KEY;
import static com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_ACCESS_TOKEN;
import static com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_BDUSS;
import static com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_LIGHT_APP_API_KEY;
import static com.baidu.android.pushservice.PushConstants.LOGIN_TYPE_UNKNOWN;

/**
 * @author Kirill Rozov
 * @since 16.10.14.
 */
public class BaiduPushProvider extends BasePushProvider {

    public static final String NAME = "Baidu Cloud Push";

    private String mApiKey;

    @LoginType
    private int mLoginType = -1;

    public BaiduPushProvider(@NonNull Context context, @LoginType int loginType, String apiKey) {
        super(context, NAME, null);
        mApiKey = apiKey;
        mLoginType = loginType;
    }

    @Override
    public void register() {
        if (mLoginType != -1) {
            PushManager.startWork(getContext(), mLoginType, mApiKey);
        } else {
//            PushManager.startWork(getContext(), null, null);
        }
    }

    @Override
    public void unregister() {
        PushManager.stopWork(getContext());
    }

    @Override
    public boolean isAvailable() {
        return super.isAvailable() && PushManager.isPushEnabled(getContext());
    }

    @Override
    public boolean isRegistered() {
        return PushManager.isConnected(getContext());
    }

    @Nullable
    @Override
    public String getRegistrationId() {
        return null;
    }

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            LOGIN_TYPE_ACCESS_TOKEN,
            LOGIN_TYPE_API_KEY,
            LOGIN_TYPE_BDUSS,
            LOGIN_TYPE_LIGHT_APP_API_KEY,
            LOGIN_TYPE_UNKNOWN,
            -1
    })
    public @interface LoginType {
    }
}
