package cn.com.magnity.coresdksample.Util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class logSave {
    private static BufferedWriter bufferedWriter;
    private static BufferedReader bufferedReader;
    private static   String path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Temp" + File.separator + "getTempratureData.txt";

    public logSave() {
    }

    public static void saveIntFfc(int[] a) {
        File file1 = Environment.getExternalStorageDirectory();
        file1 = new File(file1, "Temp/");
        if (!file1.exists()) {
            file1.mkdirs();
        }


      /*  SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
        String formatStr = formatter.format(new Date());*/

        String write = intToString(a);
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(path));
            bufferedWriter.write(write);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    public static String intToString(int[] a){
        StringBuilder stringBuilder=new StringBuilder();
        int count=0;
        for(int i=0;i<a.length;i++){
          /*  if(i%160==0){
            stringBuilder.append("\r\n");
            //count=0;
            }*/
            stringBuilder.append(a[i]).append(",");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


    public static int[] readFfc(){
        File file1 = Environment.getExternalStorageDirectory();
        file1 = new File(file1, "Temp/");
        if (!file1.exists()) {
            file1.mkdirs();
        }
        try {
            bufferedReader=new BufferedReader(new FileReader(path));
            String read=bufferedReader.readLine();
            int[] array=stringToInt(read);
            /*for(int i=0;i<array.length;i++){
                System.out.println(array[i]);
            }*/
            return array;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new int[1];
    }



    public static int[] stringToInt(String str){
        String[] strAry = str.split(",");
        int[] ary=new int[strAry.length];
        for(int i=0;i<strAry.length;i++){
            if(!strAry[i].equals("")){
                ary[i]=Integer.parseInt(strAry[i]);
            }
        }
        return ary;
    }


}
