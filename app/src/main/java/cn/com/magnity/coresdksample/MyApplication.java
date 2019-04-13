package cn.com.magnity.coresdksample;

import android.app.Application;
import android.util.Log;

import cn.com.magnity.coresdksample.Util.logSave;


public class MyApplication extends Application {
    private static final String TAG="MyApplication";
    public static logSave getTemp;//全局照片名称保存对象
    private static MyApplication myApplication = null;
    public static MyApplication getInstance() {
        return myApplication;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        Log.i(TAG, "程序启动完成: ");
        photoNameSaveLog();
    }
    private void   photoNameSaveLog() {
        getTemp=new logSave();
    }

}
