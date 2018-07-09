package com.ismummy.rsaenrollment;

import android.app.Application;
import android.os.Handler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Main application
 */

@SuppressWarnings("ALL")
public class MainApplication extends Application {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static MainApplication instance;
    public static volatile Handler applicationHandler = null;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/Lato-Regular.ttf")
                .setFontAttrId(R.attr.fontPath).build());
        applicationHandler = new Handler(instance.getMainLooper());
    }

    private static void runOnUIThread(Runnable runnable) {
        applicationHandler.post(runnable);
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static MainApplication getInstance() {
        return instance;
    }
}
