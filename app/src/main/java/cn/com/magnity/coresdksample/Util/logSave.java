package cn.com.magnity.coresdksample.Util;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class logSave {
    private static BufferedWriter bufferedWriter;

    public logSave() {
    }

    /**
     * 温度照片
     */

    public static void saveLog(String str, int[] a) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String formatStr = formatter.format(new Date());
        String num = intToString(a);
        File file1 = Environment.getExternalStorageDirectory();
        file1 = new File(file1, "Temp/");
        if (!file1.exists()) {
            file1.mkdirs();
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Temp" + File.separator + "getTempratureData.txt";
        FlieUtil.isExistFlie(path);
        File files1 = new File(path);
        FileWriter file = null;
        if (str.equals("完整温度")) {
            try {
                file = new FileWriter(files1, true);
                file.write(formatStr + ": " + num + "\r\n");
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void saveInt(int[] a) {
        File file1 = Environment.getExternalStorageDirectory();
        file1 = new File(file1, "Temp/");
        if (!file1.exists()) {
            file1.mkdirs();
        }
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Temp" + File.separator + "getTempratureData.txt";

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String formatStr = formatter.format(new Date());

        String write = intToString(a);
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(formatStr + ": " + write + "\r\n");
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static String intToString(int[] a){
        StringBuilder stringBuilder=new StringBuilder();
        int count=0;
        for(int i=0;i<a.length;i++){
            if(i%160==0){
            stringBuilder.append("\r\n");
            //count=0;
            }
            stringBuilder.append(a[i]).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


}
