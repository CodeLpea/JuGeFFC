package cn.com.magnity.coresdksample;

import android.app.Application;
import android.util.Log;


public class MyApplication extends Application {
    private static final String TAG="MyApplication";
    private static MyApplication myApplication = null;
    public static int[] FFCTemps;

    public static MyApplication getInstance() {
        return myApplication;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        Log.i(TAG, "程序启动完成: ");
    }


}
