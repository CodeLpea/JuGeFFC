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

import static cn.com.magnity.coresdksample.Util.TempUtil.m_FrameWidth;

public class SaveTemps {
  /*  private static int m_FrameHeight=120;//高120
    private static int m_FrameWidth=160;//宽度160*/
    private static BufferedWriter bufferedWriter;
    private static   String pathTemps = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Temp" + File.separator + "TempsSave.txt";



    public static void saveIntTemps(int[] a,String tempName,int MaxMinAvg[],boolean isOpenTree) {
        String staus="正常一帧：";
        if(isOpenTree){
            staus="三帧测试：";
        }
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
                    bufferedWriter.write("\r\n"+"平均值："+MaxMinAvg[2]+"  最大值：  "+MaxMinAvg[0]+"  最小值：  "+MaxMinAvg[1]+"\r\n");
                    bufferedWriter.write("\r\n"+formatStr+staus+" 与上一张的差  : "+"\r\n"+write+"\r\n");
                    break;
                case "Origin":
                    bufferedWriter.write("\r\n"+"平均值："+MaxMinAvg[2]+"  最大值：  "+MaxMinAvg[0]+"  最小值：  "+MaxMinAvg[1]+"\r\n");
                    bufferedWriter.write("\r\n"+formatStr+staus+" 原始数据  : "+"\r\n"+write+"\r\n");
                    break;
                case "FFC":
                    bufferedWriter.write("\r\n"+"平均值："+MaxMinAvg[2]+"  最大值：  "+MaxMinAvg[0]+"  最小值：  "+MaxMinAvg[1]+"\r\n");
                    bufferedWriter.write("\r\n"+formatStr+staus+" FFC数据  : "+"\r\n"+write+"\r\n");
                    break;
                case "After":
                    bufferedWriter.write("\r\n"+"平均值："+MaxMinAvg[2]+"  最大值：  "+MaxMinAvg[0]+"  最小值：  "+MaxMinAvg[1]+"\r\n");
                    bufferedWriter.write("\r\n"+formatStr+staus+" FFC校准后的数据  : "+"\r\n"+write+"\r\n");
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
