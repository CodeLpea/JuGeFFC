package cn.com.magnity.coresdksample.Util;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class FlieUtil {
    /**
     * 初始化文件夹
     * 检查，生成
     * */
    public static void initFile(String filePath) {
        File file1 = Environment.getExternalStorageDirectory();
        if (null != file1) {
            file1 = new File(file1, filePath);//file1位根目录，filePath为文件夹：名称
            if (!file1.exists()) {
                file1.mkdirs();
            }
        }
        }
    /**
     * 检查是否存在路径文件
     * */
    public static  boolean isExistFlie(String path){
        boolean turn=false;
            File file = new File(path);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                }
                turn=false;//没有，自动创建
            }else {
                turn=true;//有
            }

        return turn;
    }
    /**
     * 检查是否存在文件
     * */
    public static  boolean isExistFlie(File file){
        boolean turn=false;
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
            }
            turn=false;//没有，自动创建
        }else {
            turn=true;//有
        }

        return turn;
    }
/**
 * 检查，生成文件
 * */
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        initFile(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }
    /**
   * 清空文件内容
    * @param fileName
    */
        public static boolean clearInfoForFile(String fileName) {
            File file =new File(fileName);
            try {

                if(!file.exists()) {
                    file.createNewFile();
                }
                FileWriter fileWriter =new FileWriter(file);
                fileWriter.write("");
                fileWriter.flush();
                fileWriter.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }


    /*
     替换文本文件中的 非法字符串
 * @param path
 * @throws IOException
 */
    public static  void replacTextContent(String path,String srcStr,String replaceStr) throws IOException {
        //原有的内容srcStr
        //要替换的内容replaceStr
        // 读
        File file = new File(path);
        FileReader in = new FileReader(file);
        BufferedReader bufIn = new BufferedReader(in);
        // 内存流, 作为临时流
        CharArrayWriter tempStream = new CharArrayWriter();
        // 替换
        String line = null;
        while ( (line = bufIn.readLine()) != null) {
            // 替换每行中, 符合条件的字符串
            line = line.replaceAll(srcStr, replaceStr);
            // 将该行写入内存
            tempStream.write(line);
            // 添加换行符
            tempStream.append(System.getProperty("line.separator"));
        }
        // 关闭 输入流
        bufIn.close();
        // 将内存中的流 写入 文件
        FileWriter out = new FileWriter(file);
        tempStream.writeTo(out);
        out.close();
        System.out.println("====path:"+path);

    }
    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;// 2012年10月03日 23:41:31
    }

    /**
     * @return 文件夹（按照天自动创建文件夹）
     */
    public static String getFolderPathToday() {
        String folderpath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator + getFileName(); // 根目录:sd/ddnet/img/当天时间
        File storeFolder = new File(folderpath);
        if (!storeFolder.exists()) {
            storeFolder.mkdirs();
        }
        return folderpath;
    }

}
