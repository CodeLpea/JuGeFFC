package cn.com.magnity.coresdksample.Util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FFCUtil {
    public FFCUtil() {
    }


    /**
     * 获取到FFC
     * 原始数据，默认为拍摄一个均匀温度面
     * 算出平均值，并将每个店与平局值进行减
     * 并再减的基础上增加10度，也就是增加10000
     */
    public static int[] getFFC(int[] a) {
        int avg=intAvg(a);
        for(int i=0;i<a.length;i++){
            a[i]=a[i]-avg+10000;
        }
        //保存
        //saveFFC(a);
        return a;
    }
    /**
     * 算平均
     */
    public static int intAvg(int[] a) {
        int Total = 0;
        for(int i=0;i<a.length;i++){
            Total=Total+a[i];
        }
        Log.i("Total: ", String.valueOf((Total)));
        Log.i("a.length: ", String.valueOf((a.length)));
        Log.i("intAvg: ", String.valueOf((Total/a.length)));
        return Total/a.length;
    }



}
