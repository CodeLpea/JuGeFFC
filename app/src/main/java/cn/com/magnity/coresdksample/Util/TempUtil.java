package cn.com.magnity.coresdksample.Util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.content.ContentValues.TAG;

public class TempUtil {
    private static int m_FrameHeight=120;
    private static int m_FrameWidth=160;

    public static Bitmap CovertToBitMap(int[] date, int min, int max){

       int[] data= ReLoadY(date);

        data=ReLoadX(data);

        Bitmap bmp = null;
        //找到数组中的最大值和最小值
        int l_data_max = 33000;
        int l_data_min = 0;
        // Log.i(TAG, "l_data_max : " + (int)l_data_max + " l_data_min : " + (int)l_data_min);
        //根据红外照片值换算成对应的颜色值
        int[] imageColors = new int[m_FrameHeight * m_FrameWidth];
        int diff = (int)(l_data_max - l_data_min + 1);
        diff = ((diff < 256) ? 256 : diff);
        int colormap_size = ColorMap.colorTable.length;
        for (int imageRow = 0; imageRow < m_FrameHeight; ++imageRow)
        {
            for (int imageCol = 0; imageCol < m_FrameWidth; ++imageCol)
            {
                int baseValue = data[m_FrameWidth * imageRow  + imageCol]; // take input value in [0, 65536)
                char scaledValue = (char)(256 * (baseValue - l_data_min) / diff); // map value to interval [0, 256), and set the pixel to its color value above
                int index = 3 * scaledValue;
                if(index < (colormap_size - 2))
                {
                    imageColors[m_FrameWidth * imageRow  + imageCol] = Color.rgb(ColorMap.colorTable[index],
                            ColorMap.colorTable[index + 1],
                            ColorMap.colorTable[index + 2]);
                }
            }
        }
        bmp = Bitmap.createBitmap(imageColors,m_FrameWidth, m_FrameHeight, Bitmap.Config.ARGB_8888);
        return bmp;
    }

    private static int[] ReLoadX(int[] data) {
        //进行X轴的转换
        //进行X轴的转换
        int j=0;
        int[][]b=new int[120][160];
        for(int i=0;i<data.length;i++){
            if(i%160==0&&i!=0){
                j=j+1;
            }
            b[j][159-i%160]=data[i];
        }
        j=0;
        for(int i=0;i<data.length;i++){
            if(i%160==0&&i!=0){
                j=j+1;
            }
            data[i]=b[j][i%160];
        }

        return data;
    }

    /**将数组重新排序*/
    private static int[] ReLoadY(int[] a) {
        int[] data=new int[a.length];
        int j=0;
        //进行Y轴的反转
        for(int i=a.length-1;i>0;i--){
            data[i]=a[j++];
        }
        return data;
    }

    /** 保存方法 */
    public static void saveBitmap(Bitmap bm,String picName) {
        Log.e(TAG, "保存图片");
        File f = new File(Environment.getExternalStorageDirectory(), picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static int[] MaxMinTemp(int temps[]){
        int []MaxMin=new int[2];
        int i,min,max;

        min=max=temps[0];
        for(i=0;i<temps.length;i++)
        {
            if(temps[i]>max)   // 判断最大值
                max=temps[i];
            if(temps[i]<min)   // 判断最小值
                min=temps[i];
        }
        System.out.println("\nTemp最大值是："+max); // 输出最大值
        System.out.println("Temp最小值是："+min); // 输出最小值
        MaxMin[0]=max;
        MaxMin[1]=min;
        return MaxMin;
    }

}
