package cn.com.magnity.coresdksample.Util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SaveTemps {
    private static int m_FrameHeight=120;//高120
    private static int m_FrameWidth=160;//宽度160
    private static BufferedWriter bufferedWriter;
    private static   String pathTemps = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Temp" + File.separator + "TempsSave.txt";



    public static void saveIntTemps(int[] a,String tempName) {
        File file1 = Environment.getExternalStorageDirectory();
        file1 = new File(file1, "Temp/");
        if (!file1.exists()) {
            file1.mkdirs();
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String formatStr = formatter.format(new Date());
        String write = intToString(a);
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(pathTemps,true));
            switch (tempName){
                case "Compare":
                    bufferedWriter.write("\r\n"+formatStr+" 与上一张的差  : "+"\r\n"+write+"\r\n");
                    break;
                case "Origin":
                    bufferedWriter.write("\r\n"+formatStr+" 原始数据  : "+"\r\n"+write+"\r\n");
                    break;
                case "FFC":
                    bufferedWriter.write("\r\n"+formatStr+" FFC数据  : "+"\r\n"+write+"\r\n");
                    break;
                case "After":
                    bufferedWriter.write("\r\n"+formatStr+" FFC校准后的数据  : "+"\r\n"+write+"\r\n");
                    break;
            }
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }



    public static String intToString(int[] a){
        StringBuilder stringBuilder=new StringBuilder();
        int count=0;
        for(int i=0;i<a.length;i++){
            if(i%m_FrameWidth==0){
            stringBuilder.append("\r\n");
            //count=0;
            }
            stringBuilder.append(a[i]).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }





}
