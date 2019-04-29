package cn.com.magnity.coresdksample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import cn.com.magnity.coresdk.MagDevice;
import cn.com.magnity.coresdk.types.CameraInfo;
import cn.com.magnity.coresdk.types.StatisticInfo;
import cn.com.magnity.coresdksample.Util.FFCUtil;
import cn.com.magnity.coresdksample.Util.TempUtil;

import static cn.com.magnity.coresdksample.MyApplication.FFCTemps;
import static cn.com.magnity.coresdksample.MyApplication.locationAny;
import static cn.com.magnity.coresdksample.Util.TempUtil.m_FrameHeight;
import static cn.com.magnity.coresdksample.Util.TempUtil.m_FrameWidth;


public class MagSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG="MagSurfaceView";
    private volatile boolean mIsDrawing;
    private DrawThread mDrawThread;

    private volatile boolean mIsSignaled;
    private ReentrantLock mLock;
    private Condition mNewFrameCond;

    private MagDevice mDev;

    /* avoid continuously alloc new memory*/
    private Rect mDstRect;
    private StatisticInfo mStatisticInfo;
    private CameraInfo mCameraInfo;

    private Paint mPaint;

    private PaintFlagsDrawFilter mPfd;

    public MagSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
    }
    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                float x=event.getX()/getWidth();
                float y=event.getY()/getHeight();
                float xx=x*160;
                float YY=y*120;
                locationAny[0]= (int)xx;
                locationAny[1]= (int)YY;
                break;
        }
        return super.onTouchEvent(event);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(3);
        mPaint.setTextSize(20);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

        locationAny=new int[2];

        /* bilinear */
        mPfd = new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mPaint = null;
        mPfd = null;
    }

    public void invalidate_() {
        if (!mIsDrawing) {
            return;
        }

        mLock.lock();
        mIsSignaled = true;
        mNewFrameCond.signal();
        mLock.unlock();
    }

    public void startDrawingThread(MagDevice dev) {
        if (mIsDrawing) {
            return;
        }

        mDrawThread = new DrawThread();
        mLock = new ReentrantLock();
        mNewFrameCond = mLock.newCondition();

        mDev = dev;
        mStatisticInfo = new StatisticInfo();

        mCameraInfo = new CameraInfo();
        mDev.getCameraInfo(mCameraInfo);

        /* 4 : 3 is default, but not always because of rotate */
        if (mCameraInfo.bmpWidth * 3 == mCameraInfo.bmpHeight * 4) {
            mDstRect = new Rect(0, 0, getWidth(), getHeight());
        } else {
            int h = getHeight();
            int w = h * mCameraInfo.bmpWidth / mCameraInfo.bmpHeight;
            int dx = (getWidth() - w) / 2;
            mDstRect.set(dx, 0, dx + w, h);
        }

        /* start drawing thread */
        mIsDrawing = true;
        mIsSignaled = false;
        mDrawThread.start();
    }

    public void stopDrawingThread() {
        if (!mIsDrawing) {
            return;
        }

        mIsDrawing = false;

        mLock.lock();
        mIsSignaled = true;
        mNewFrameCond.signal();
        mLock.unlock();

        try {
            mDrawThread.join();
        } catch (InterruptedException ex) {
        } finally {
            mDrawThread = null;
        }

        mDev = null;
    }

    private void drawImage(Canvas canvas, Rect dstRect, CameraInfo cameraInfo,
                           StatisticInfo info, Paint paint) {
      /*  Bitmap bmp;


        mDev.lock();
        bmp = mDev.getOutputImage();
        mDev.getFrameStatisticInfo(info);
        mDev.getCameraInfo(cameraInfo);
        mDev.unlock();
        if (bmp != null) {
            canvas.drawBitmap(bmp, null, dstRect, null);
            drawMaxTemp(canvas, dstRect, cameraInfo, info, paint);
            drawMinTemp(canvas, dstRect, cameraInfo, info, paint);
            // drawAnyTemp(canvas, dstRect, cameraInfo, info, paint);
        }*/

        showTemperatureDataVideo(canvas, dstRect, cameraInfo, info, paint);//使用原始数据作为视频流


    }
/**
 * 使用原始数据作为视频流
 * */
    private void showTemperatureDataVideo(Canvas canvas, Rect dstRect, CameraInfo cameraInfo, StatisticInfo info, Paint paint) {
        mDev.lock();
        int[] temps = new int[160*120];
        mDev.getTemperatureData(temps,true,true);
        mDev.unlock();
        int []AfterTemps=new int[temps.length];

        /*if(FFCTemps==null){//如果没有缓存
            Log.i(TAG, "读取到本地FFC文件: ");
            FFCTemps= FFCUtil.readFfc();//读取本地
        }

        if(FFCTemps.length>10){//本地读取到有效的FFC
            for(int i=0;i<AfterTemps.length;i++){
                AfterTemps[i]=temps[i]-FFCTemps[i];
            } //将原始数据通过FFc数据处理

        }else {//本地没有读取到有效的FFCTemps，就直接不做FFC处理，采用原始的temps
           AfterTemps=temps;
            Log.i(TAG, "本地没有FFC数据");
        }*/
        AfterTemps=temps;

        final Bitmap bitmap;
        bitmap= TempUtil.CovertToBitMap(AfterTemps,0,100);

        Bitmap bitmap1=bitmap.copy(Bitmap.Config.ARGB_8888, true);

        canvas.drawBitmap(bitmap1, null, dstRect, null);


        AfterTemps=TempUtil.ReLoad(AfterTemps);//旋转原始数据，x，y都旋转
        int []maxTemp=TempUtil.DDNgetRectTemperatureInfo(AfterTemps,0,m_FrameWidth,0,m_FrameHeight);//获取指定矩形区域中最大的值
        int []anyTemp=TempUtil.DDNgetAnyTemperatureInfo(AfterTemps,locationAny[0],locationAny[1]);//获取指定矩形区域中最大的值

        drawMaxTemp(canvas, dstRect, cameraInfo, maxTemp, paint);
        drawAnyTemp(canvas, dstRect, cameraInfo, anyTemp, paint);
    }

    /**
 * 获取任意位置的温度
 * */
    private void drawAnyTemp(Canvas canvas, Rect dstRect, CameraInfo cameraInfo, int []info, Paint paint) {

        int temp =info[0];
        int yFPA =info[2];
        int xFPA =info[1];
        Paint paint2=paint;
        paint2.setColor(Color.BLUE);


   /*     Log.i(TAG, "原始info[2]Y: "+info[2]);
        Log.i(TAG, "原始info[1]X: "+info[1]);*/

     /*   Log.i(TAG, "处理后xFPA: "+xFPA);
        Log.i(TAG, "处理后yFPA: "+yFPA);*/


        //convert to the screen coordinate
        int x = xFPA * dstRect.width() / cameraInfo.fpaWidth + dstRect.left;
        int y = /*dstRect.height() -*/ yFPA * dstRect.height() / cameraInfo.fpaHeight + dstRect.top;

/*
        Log.i(TAG, "显示的 x: "+x);
        Log.i(TAG,  "显示的 y: "+y);*/

        /* text to show */
        String s = String.format(Locale.ENGLISH, "%.1fC", temp * 0.001f);




        /* FIXME: allocate new object in high frequently running function */
        Rect rt = new Rect();
        mPaint.getTextBounds(s, 0, s.length(), rt);

        final int pad = 10;
        final int lineWidth = 8;
        int cx = rt.width();
        int cy = rt.height();

        /* draw cross for max temp point */
        canvas.drawLine(x - lineWidth, y, x + lineWidth, y, paint2);
        canvas.drawLine(x, y - lineWidth, x, y + lineWidth, paint2);

        /* draw text */
        x += pad;
        y += cy + pad;
        if (x > dstRect.width() - cx) {
            x -= pad * 2 + cx;
        }
        if (y > dstRect.height()) {
            y -= pad * 2 + cy * 2;
        }



        canvas.drawText(s, x, y, paint2);


    }

    private void drawMaxTemp(Canvas canvas, Rect dstRect, CameraInfo cameraInfo,
                             int info[], Paint paint) {



        //get the fpa coordinate
       /* int temp=info.maxTemperature;
        int yFPA = info.maxPos / cameraInfo.fpaWidth;
        int xFPA = info.maxPos - yFPA * cameraInfo.fpaWidth;*/

        int temp =info[0];
        int yFPA =info[2];
        int xFPA =info[1];

        int mintemp =info[3];
        int minyFPA =info[5];
        int minxFPA =info[4];

        Paint paintMin=paint;
        paintMin.setColor(Color.GREEN);

        Paint  paintMax = new Paint();
        paintMax.setStyle(Paint.Style.FILL);
        paintMax.setStrokeWidth(3);
        paintMax.setTextSize(20);
        paintMax.setColor(Color.RED);
        paintMax.setStrokeCap(Paint.Cap.ROUND);

   /*     Log.i(TAG, "原始info[2]Y: "+info[2]);
        Log.i(TAG, "原始info[1]X: "+info[1]);*/

     /*   Log.i(TAG, "处理后xFPA: "+xFPA);
        Log.i(TAG, "处理后yFPA: "+yFPA);*/


        //convert to the screen coordinate
        int x = xFPA * dstRect.width() / cameraInfo.fpaWidth + dstRect.left;
        int y = /*dstRect.height() -*/ yFPA * dstRect.height() / cameraInfo.fpaHeight + dstRect.top;
        int minx = minxFPA * dstRect.width() / cameraInfo.fpaWidth + dstRect.left;
        int miny = /*dstRect.height() -*/ minyFPA * dstRect.height() / cameraInfo.fpaHeight + dstRect.top;
/*
        Log.i(TAG, "显示的 x: "+x);
        Log.i(TAG,  "显示的 y: "+y);*/

        /* text to show */
        String s = String.format(Locale.ENGLISH, "%.1fC", temp * 0.001f);
        String mins = String.format(Locale.ENGLISH, "%.1fC", mintemp * 0.001f);



        /* FIXME: allocate new object in high frequently running function */
        Rect rt = new Rect();
        mPaint.getTextBounds(s, 0, s.length(), rt);

        Rect minrt = new Rect();
        mPaint.getTextBounds(mins, 0, mins.length(), minrt);



        final int pad = 10;
        final int lineWidth = 8;
        int cx = rt.width();
        int cy = rt.height();

        int mincx = minrt.width();
        int mincy = minrt.height();

        /* draw cross for max temp point */
        canvas.drawLine(x - lineWidth, y, x + lineWidth, y, paintMax);
        canvas.drawLine(x, y - lineWidth, x, y + lineWidth, paintMax);


        canvas.drawLine(minx - lineWidth, miny, minx + lineWidth, miny, paintMin);
        canvas.drawLine(minx, miny - lineWidth, minx, miny + lineWidth, paintMin);



        /* draw text */
        x += pad;
        y += cy + pad;
        if (x > dstRect.width() - cx) {
            x -= pad * 2 + cx;
        }
        if (y > dstRect.height()) {
            y -= pad * 2 + cy * 2;
        }

        /* draw text */
        minx += pad;
        miny += mincy + pad;
        if (minx > dstRect.width() - mincx) {
            minx -= pad * 2 + mincx;
        }
        if (miny > dstRect.height()) {
            miny -= pad * 2 + mincy * 2;
        }

        canvas.drawText("max:"+s, x, y, paintMax);
        canvas.drawText("min:"+mins, minx, miny, paintMin);


    }
    private void drawMinTemp(Canvas canvas, Rect dstRect, CameraInfo cameraInfo, StatisticInfo info, Paint paint) {
        int minTemp=info.minTemperature;


        int minyFPA = info.minPos / cameraInfo.fpaWidth;
        int minxFPA = info.minPos - minyFPA * cameraInfo.fpaWidth;


        int minx=minxFPA* dstRect.width() / cameraInfo.fpaWidth + dstRect.left;
        int miny=dstRect.height() - minyFPA * dstRect.height() / cameraInfo.fpaHeight + dstRect.top;

        /* text to show */
        String mins = String.format(Locale.ENGLISH, "%.1fC", minTemp * 0.001f);

        /* FIXME: allocate new object in high frequently running function */
        Rect rt = new Rect();
        mPaint.getTextBounds(mins, 0, mins.length(), rt);

        final int pad = 10;
        final int lineWidth = 8;
        int cx = rt.width();
        int cy = rt.height();

        /* draw cross for max temp point */
        canvas.drawLine(minx - lineWidth, miny, minx + lineWidth, miny, paint);
        canvas.drawLine(minx, miny - lineWidth, minx, miny + lineWidth, paint);

        /* draw text */
        minx += pad;
        miny += cy + pad;
        if (minx > dstRect.width() - cx) {
            minx -= pad * 2 + cx;
        }
        if (miny > dstRect.height()) {
            miny -= pad * 2 + cy * 2;
        }
        canvas.drawText(mins, minx, miny, paint);
    }

    void drawBackground(Canvas canvas, Paint paint) {
        paint.setColor(Color.BLACK);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        paint.setColor(Color.GREEN);
    }

    private class DrawThread extends Thread {
        @Override
        public void run() {
            while(mIsDrawing) {
                /* wait for a new frame coming */
                try {
                    mLock.lock();
                    while (!mIsSignaled) {
                        mNewFrameCond.await();
                    }
                    mIsSignaled = false;
                } catch (InterruptedException ex) {
                } finally {
                    mLock.unlock();
                }

                if (!mIsDrawing) {
                    break;
                }

                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    canvas.setDrawFilter(mPfd);
                    drawBackground(canvas, mPaint);
                    drawImage(canvas, mDstRect, mCameraInfo, mStatisticInfo, mPaint);
                    getHolder().unlockCanvasAndPost(canvas);
                }
            } //while
        }
    }
}
