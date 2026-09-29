package com.alazzi.grocery;

import android.app.Application;

public class App extends Application {
    @Override public void onCreate() {
        super.onCreate();
        AppDatabase.get(this).getWritableDatabase();
    }
}
