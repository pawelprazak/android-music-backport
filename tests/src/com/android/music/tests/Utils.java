package com.android.music.tests;

/**
 * Utility class for tests
 */
public class Utils {
    public static class Log {
        public static void i(String msg) { android.util.Log.i(TAG, msg); }
        public static void w(String msg) { android.util.Log.w(TAG, msg); }
        public static void e(String msg) { android.util.Log.e(TAG, msg); }
        public static void d(String msg) { android.util.Log.e(TAG, msg); }
        public static void v(String msg) { android.util.Log.v(TAG, msg); }
    }
    public static final String TAG = "MusicTests";
    private static final int WAIT_SHORT_TIME = 1000;
    private static final int WAIT_LONG_TIME = 2000;
    private static final int WAIT_VERY_LONG_TIME = 6000;

    public static void waitShortTime() { wait(WAIT_SHORT_TIME); }
    public static void waitLongTime() { wait(WAIT_LONG_TIME); }
    public static void waitVeryLongTime() { wait(WAIT_VERY_LONG_TIME); }
    
    public static void wait(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException ignored) {
            Log.v("wait interrupted");
        }
    }
}
