package com.example.filezip2;

import android.app.Application;
import android.content.Context;
import android.util.Log;

/**
 *
 * 用与弹窗显示
 *
 */

public class InitApplication extends Application {

    private static Application mApplication;
    private static Context sApplicationContext;
    private final boolean isDebug = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("ljj","onCreate:  ");
        mApplication = this;
        sApplicationContext = getApplicationContext();

    }


    public static Application getApplication() {
        return mApplication;
    }

}
