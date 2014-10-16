package org.onepf.opfpush.baidu;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private static final String KEY_BIND_FLAG = "bind_flag";
    protected final SharedPreferences mSharedPreferences;

    public Settings(Context context){
        mSharedPreferences = context.getSharedPreferences("baidu_pref", Context.MODE_PRIVATE);
    }

    // With the share preference to realize switch is binding.
    // And set up true success in ionBind, setting false unBind and successful
    public boolean isBinded() {
        return mSharedPreferences.getBoolean(KEY_BIND_FLAG, false);
    }

    public void saveBind(boolean flag) {
        mSharedPreferences.edit().putBoolean(KEY_BIND_FLAG, flag).apply();
    }

}
