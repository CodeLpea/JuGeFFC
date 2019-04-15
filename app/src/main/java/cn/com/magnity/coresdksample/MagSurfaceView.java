package cn.com.magnity.coresdksample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
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
import cn.com.magnity.coresdksample.Util.TempUtil;


public class MagSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
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
    public void surfaceCreated(SurfaceHolder holder) {

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(6);
        mPaint.setTextSize(50);
        mPaint.setColor(Color.GREEN);
        mPaint.setStrokeCap(Paint.Cap.ROUND);

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
        Bitmap bmp;


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
        }

        //showTemperatureDataVideo(canvas,dstRect);//使用原始数据作为视频流


    }
/**
 * 使用原始数据作为视频流
 * */
    private void showTemperatureDataVideo(Canvas canvas, Rect dstRect) {
             mDev.lock();
        int[] temps = new int[160*120];
        mDev.getTemperatureData(temps,false,false);
        mDev.unlock();
        final Bitmap bitmap;
        bitmap= TempUtil.CovertToBitMap(temps,0,100);
        Bitmap bitmap1=bitmap.copy(Bitmap.Config.ARGB_8888, true);
        canvas.drawBitmap(bitmap1, null, dstRect, null);
    }

    /**
 * 获取任意位置的温度
 * */
    private void drawAnyTemp(Canvas canvas, Rect dstRect, CameraInfo cameraInfo, StatisticInfo info, Paint paint) {
        int [] infos=new int[5];
        int [] infos2=new int[5];
        /*boolean result=mDev.getRectTemperatureInfo(10,10,50,50,infos);*/
        int result=mDev.getTemperatureProbe(50,50,1);
        Log.i("drawAnyTemp result", String.valueOf(result));
      //  Log.i("drawAnyTemp MaxTemp: ", String.valueOf(infos[1]));
       /* boolean result2=mDev.getRectTemperatureInfo(60,60,100,100,infos2);*/
        int result2=mDev.getTemperatureProbe(100,100,1);
        Log.i("drawAnyTemp result2", String.valueOf(result2));
        //Log.i("drawAnyTemp MaxTemp2: ", String.valueOf(infos[1]));


    }

    private void drawMaxTemp(Canvas canvas, Rect dstRect, CameraInfo cameraInfo,
                             StatisticInfo info, Paint paint) {
        int temp = info.maxTemperature;


        //get the fpa coordinate
        int yFPA = info.maxPos / cameraInfo.fpaWidth;
        int xFPA = info.maxPos - yFPA * cameraInfo.fpaWidth;


        //convert to the screen coordinate
        int x = xFPA * dstRect.width() / cameraInfo.fpaWidth + dstRect.left;
        int y = dstRect.height() - yFPA * dstRect.height() / cameraInfo.fpaHeight + dstRect.top;

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
        canvas.drawLine(x - lineWidth, y, x + lineWidth, y, paint);
        canvas.drawLine(x, y - lineWidth, x, y + lineWidth, paint);


        /* draw text */
        x += pad;
        y += cy + pad;
        if (x > dstRect.width() - cx) {
            x -= pad * 2 + cx;
        }
        if (y > dstRect.height()) {
            y -= pad * 2 + cy * 2;
        }
        canvas.drawText(s, x, y, paint);


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
