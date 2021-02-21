package com.example.da_chuang;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {
    private static Context context;

    //获取应用上下文环境
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }
}
