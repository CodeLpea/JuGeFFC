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
/**
 * int[]温度数据的xy反转
 *
 * 获取指定矩形区域的最大最小值
 * */
public class TempUtil {
    public static int m_FrameHeight=120;//高120
    public static int m_FrameWidth=160;//宽度160

    public static Bitmap CovertToBitMap(int[] date, int min, int max){

        int[] data= ReLoadY(date);

        data=ReLoadX(data);

        int maxmin[]= TempUtil.MaxMinTemp(data);//找出最大最小值

        Bitmap bmp = null;
        //找到数组中的最大值和最小值
        int l_data_max = maxmin[0];
        int l_data_min = maxmin[1];
        // Log.i(TAG, "l_data_max : " + (int)l_data_max + " l_data_min : " + (int)l_data_min);
        //根据红外照片值换算成对应的颜色值
        int[] imageColors = new int[m_FrameHeight * m_FrameWidth];
        int diff = (int)(l_data_max - l_data_min + 1);
        //diff = ((diff < 256) ? 256 : diff);
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
    public static int[] ReLoad(int[] data) {
         data= ReLoadY(data);
         data=ReLoadX(data);
         return data;
    }

    private static int[] ReLoadX(int[] data) {
        //进行X轴的转换
        //进行X轴的转换
        int j=0;
        int[][]b=new int[m_FrameHeight][m_FrameWidth];
        for(int i=0;i<data.length;i++){
            if(i%m_FrameWidth==0&&i!=0){
                j=j+1;
            }
            b[j][(m_FrameWidth-1)-i%m_FrameWidth]=data[i];
        }
        j=0;
        for(int i=0;i<data.length;i++){
            if(i%m_FrameWidth==0&&i!=0){
                j=j+1;
            }
            data[i]=b[j][i%m_FrameWidth];
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
        int []MaxMin=new int[3];
        int i,min,max,total = 0,avg;

        min=max=temps[0];
        for(i=0;i<temps.length;i++)
        {
            if(temps[i]>max)   // 判断最大值
                max=temps[i];
            if(temps[i]<min)   // 判断最小值
                min=temps[i];
            total=total+temps[i];
        }
        avg=total/temps.length;
      /*  Log.i(TAG, "\nTemp最大值是： "+max); // 输出最大值
        Log.i(TAG, "Temp最小值是：   "+min); // 输出最小值*/
        MaxMin[0]=max;
        MaxMin[1]=min;
        MaxMin[2]=avg;
        return MaxMin;
    }
/**
 * 根据原始温度temps
 * 获取指定区域的最高温度
 * x:0-160 m_FrameWidth
 * y:0-120 m_FrameHeight
 * */
    public static int[] DDNgetRectTemperatureInfo(int[] temps,int x0,int x1,int y0,int y1){

        int [] Maxresult=new int[6];

        int[][]b=new int[m_FrameWidth][m_FrameHeight];
        for(int i=0;i<temps.length;i++){
            b[i%m_FrameWidth][i/m_FrameWidth]=temps[i];//将一维数组转换为2维数组，坐标原点为（0，0）
        }
        Maxresult[3]=b[0][0];
        for (int i = x0; i < x1; i++) {
            for (int k= y0; k < y1; k++) {
                if (Maxresult[0] < b[i][k]) {
                    Maxresult[0] = b[i][k];//算出最大值
                    Maxresult[1]=i;//最大值的位置x
                    Maxresult[2]=k;//最大值的位置y
                }
                if (Maxresult[3] > b[i][k]&&b[i][k]!=0) {
                    Maxresult[3] = b[i][k];//算出最小值
                    Maxresult[4] = i;//算出最小值的位置x
                    Maxresult[5] = k;//算出最小值的位置y

                }

            }

        }

        return Maxresult;
    }
    /**
     * 获取每个点的温度与平均值的绝对值的和的平均值。
     * */
    public static int DDNgetAvgTemperatureInfo(int[] temps){
        int getAvg = 0;
        int abstotal=0;
        int total=0;
        for(int i=0;i<temps.length;i++){
            total=temps[i]+total;
        }
        int Avg=total/temps.length;
        for(int i=0;i<temps.length;i++){
            abstotal=Math.abs(temps[i]-Avg)+abstotal;
        }
        getAvg=abstotal/temps.length;
        return getAvg;
    }

    /**
     * 九个点的平均值最大的区域
     * */
    public static int NineMaxAvgTemp(int x,int y,int temps[][]){
        int area=4;//点到周围点的距离
        int maxAvg=0;
        int xlen=x%m_FrameWidth;//x点距离边界的距离
        int ylen=y%m_FrameHeight;//y点距离边界的距离

        if(xlen>area&&ylen>area&&xlen<m_FrameWidth-area&&ylen<m_FrameHeight-area){//不在边界时候
            maxAvg=(temps[x][y]+temps[x][y-1]+temps[x][y+1]//中间列
                    +temps[x-1][y-1]+temps[x-1][y]+temps[x-1][y+1]//左侧列
                    +temps[x+1][y-1]+temps[x+1][y]+temps[x+1][y+1])/9;//右侧列
        }
        return maxAvg;
    }

    /**
     * 获取任意位置的温度
     * */
    public static int[] DDNgetAnyTemperatureInfo(int[] temps,int x,int y){

        int []anyTemp=new int[3];

        int[][]b=new int[m_FrameWidth][m_FrameHeight];
        for(int i=0;i<temps.length;i++){
            b[i%m_FrameWidth][i/m_FrameWidth]=temps[i];//将一维数组转换为2维数组，坐标原点为（0，0）
        }
        anyTemp[0]=b[x][y];
        anyTemp[1]=x;
        anyTemp[2]=y;

      /*  Log.i(TAG, "\nMaxresult[1] XXXXXXXXX：         "+Maxresult[1]); // 输出X值
        Log.i(TAG, "Maxresult[2]=i YYYYYYYYY：         "+Maxresult[2]); // 输出Y值
        Log.i(TAG, "Maxresult[0]=i MMMMMMMMM：         "+Maxresult[0]); // 输出值
*/
        return anyTemp;
    }


}
