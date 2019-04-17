package cn.com.magnity.coresdksample;

import android.Manifest;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;

import cn.com.magnity.coresdk.MagDevice;
import cn.com.magnity.coresdk.types.EnumInfo;
import cn.com.magnity.coresdksample.Util.FFCUtil;
import cn.com.magnity.coresdksample.Util.SaveTemps;
import cn.com.magnity.coresdksample.Util.TempUtil;

import static cn.com.magnity.coresdksample.MyApplication.FFCTemps;
import static cn.com.magnity.coresdksample.Util.FFCUtil.getFFC;
import static cn.com.magnity.coresdksample.Util.TempUtil.m_FrameHeight;
import static cn.com.magnity.coresdksample.Util.TempUtil.m_FrameWidth;

public class MainActivity extends AppCompatActivity implements MagDevice.ILinkCallback {
    private static final String TAG = "MainActivity";
    //const
    private static final int START_TIMER_ID = 0;
    private static final int TIMER_INTERVAL = 500;//ms

    private static final int STATUS_IDLE = 0;
    private static final int STATUS_LINK = 1;
    private static final int STATUS_TRANSFER = 2;

    private static final String STATUS_ARGS = "status";
    private static final String USBID_ARGS = "usbid";

    //non-const
    private MagDevice mDev;

    private ArrayList<EnumInfo> mDevices;
    private ArrayList<String> mDeviceStrings;
    private ArrayAdapter mListAdapter;
    private EnumInfo mSelectedDev;
    private ListView mDevList;
    private Button mLinkBtn;
    private Button mPlayBtn;
    private Button mCalibrationBtn;
    private Button mCurrentBtn;
    private Button mFFCBtn;
    private Button mAfterBtn;
    private TextView mTextSelectedDevice;
    private Handler mEnumHandler;
    private Handler mRestoreHandler;
    private Runnable mRestoreRunnable;
    private VideoFragment mVideoFragment;

    private ImageView iv_origin,iv_FFC,iv_after, iv_compare;
    private TextView tv_origin,tv_FFC,tv_after,tv_current;



    private int mDegree;//0 - 90, 1 - 180, 2 - 270

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTemp(savedInstanceState);

        /* init ui */
        initUi();

        initMyUi();

    }

    private void initMyUi() {
        iv_origin= (ImageView) findViewById(R.id.iv_origin);
        iv_FFC= (ImageView) findViewById(R.id.iv_FFC);
        iv_after= (ImageView) findViewById(R.id.iv_after);
        iv_compare = (ImageView) findViewById(R.id.iv_Compare);

        tv_origin=(TextView)findViewById(R.id.tv_origin);
        tv_FFC=(TextView)findViewById(R.id.tv_FFC);
        tv_after=(TextView)findViewById(R.id.tv_after);
        tv_current=(TextView)findViewById(R.id.tv_current);



    }

    private void initTemp(Bundle savedInstanceState) {

        /* global init */
        MagDevice.init(this);

        /* new object */
        mDev = new MagDevice();
        mDevices = new ArrayList<>();
        mDeviceStrings = new ArrayList<>();
        mListAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, mDeviceStrings);

        /* enum timer handler */
        mEnumHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case START_TIMER_ID:
                        mEnumHandler.removeMessages(START_TIMER_ID);
                        updateDeviceList();
                        mEnumHandler.sendEmptyMessageDelayed(START_TIMER_ID, TIMER_INTERVAL);
                        break;
                }
            }
        };

        /* start timer */
        mEnumHandler.sendEmptyMessage(START_TIMER_ID);

        /* runtime permit */
        Utils.requestRuntimePermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, 0, R.string.writeSDPermission);

        /* restore parameter */
        if (savedInstanceState != null) {
            final int usbId = savedInstanceState.getInt(USBID_ARGS);
            final int status = savedInstanceState.getInt(STATUS_ARGS);
            mRestoreHandler = new Handler();
            mRestoreRunnable = new Runnable() {
                @Override
                public void run() {
                    restore(usbId, status);
                }
            };

            /* restore after all ui component created */
            /* FIXME */
            mRestoreHandler.postDelayed(mRestoreRunnable, 200);
        }

    }

    private void initUi() {
        mDevList = (ListView)findViewById(R.id.listDev);
        mDevList.setAdapter(mListAdapter);
        mDevList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EnumInfo dev = mDevices.get(position);
                if (mSelectedDev == null ||
                        mSelectedDev.id != dev.id ||
                        !mDev.isLinked()) {
                    mDev.dislinkCamera();
                    mSelectedDev = dev;
                    mTextSelectedDevice.setText(mSelectedDev.name);
                    updateButtons();
                }
            }
        });

        MagOnClickListener listener = new MagOnClickListener();
        mLinkBtn = (Button)findViewById(R.id.btnLink);
        mLinkBtn.setOnClickListener(listener);
        mPlayBtn = (Button)findViewById(R.id.btnPlay);
        mPlayBtn.setOnClickListener(listener);
        mCalibrationBtn = (Button)findViewById(R.id.btnOrigin);
        mCalibrationBtn.setOnClickListener(listener);
        mCurrentBtn = (Button)findViewById(R.id.btnCunrrent);
        mCurrentBtn.setOnClickListener(listener);
        mFFCBtn = (Button)findViewById(R.id.btnFFC);
        mFFCBtn.setOnClickListener(listener);
        mAfterBtn = (Button)findViewById(R.id.btnafter);
        mAfterBtn.setOnClickListener(listener);
        mTextSelectedDevice = (TextView) findViewById(R.id.tvSelectedName);

        updateButtons();

        FragmentManager fm = getSupportFragmentManager();
        mVideoFragment = (VideoFragment)fm.findFragmentById(R.id.videoLayout);
        if (mVideoFragment == null) {
            mVideoFragment = new VideoFragment();
            fm.beginTransaction().add(R.id.videoLayout, mVideoFragment).commit();
        }



    }

    private void restore(int usbId, int status) {
        /* restore list status */
       MagDevice.getDevices(this, 33596, 1, mDevices);

        mDeviceStrings.clear();
        for (EnumInfo dev : mDevices) {
            if (dev.id == usbId) {
                mSelectedDev = dev;
            }
            mDeviceStrings.add(dev.name);
        }
        if (mSelectedDev == null) {
            return;
        }

        mTextSelectedDevice.setText(mSelectedDev.name);

        /* restore camera status */
        switch (status) {
            case STATUS_IDLE:
                //do nothing
                break;
            case STATUS_LINK:
                mDev.linkCamera(MainActivity.this, mSelectedDev.id, MainActivity.this);
                updateButtons();
                break;
            case STATUS_TRANSFER:
                int r = mDev.linkCamera(MainActivity.this, mSelectedDev.id,
                    new MagDevice.ILinkCallback() {
                        @Override
                        public void linkResult(int result) {
                            if (result == MagDevice.CONN_SUCC) {
                            /* 连接成功 */
                                play();
                            } else if (result == MagDevice.CONN_FAIL) {
                            /* 连接失败 */
                            } else if (result == MagDevice.CONN_DETACHED) {
                            /* 连接失败*/
                            }
                            updateButtons();
                        }
                    });

                if (r == MagDevice.CONN_SUCC) {
                    play();
                }
                updateButtons();
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        /* save parameter for restore when screen rotating */
        int status = STATUS_IDLE;
        if (mDev.isProcessingImage()) {
            status = STATUS_TRANSFER;
        } else if (mDev.isLinked()) {
            status = STATUS_LINK;
        }
        outState.putInt(STATUS_ARGS, status);
        if (mSelectedDev != null) {
            outState.putInt(USBID_ARGS, mSelectedDev.id);
        }
    }

    private void updateDeviceList() {
        MagDevice.getDevices(this, 33596, 1, mDevices);

        mDeviceStrings.clear();
        for (EnumInfo dev : mDevices) {
            mDeviceStrings.add(dev.name);
        }

        mListAdapter.notifyDataSetChanged();
    }

    private void updateButtons() {
        if (mDev.isProcessingImage()) {
            mLinkBtn.setEnabled(false);
            mPlayBtn.setEnabled(false);
            mCalibrationBtn.setEnabled(true);
            mCurrentBtn.setEnabled(true);
            mFFCBtn.setEnabled(true);
            mAfterBtn.setEnabled(true);
        } else if (mDev.isLinked()) {
            mLinkBtn.setEnabled(false);
            mPlayBtn.setEnabled(true);
            mCalibrationBtn.setEnabled(true);
            mCurrentBtn.setEnabled(true);
            mFFCBtn.setEnabled(true);
            mAfterBtn.setEnabled(false);
        } else {
            mLinkBtn.setEnabled(mSelectedDev!=null);
            mPlayBtn.setEnabled(false);
            mCalibrationBtn.setEnabled(false);
            mCurrentBtn.setEnabled(false);
            mFFCBtn.setEnabled(false);
            mAfterBtn.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        /* remove pending messages */
        mEnumHandler.removeCallbacksAndMessages(null);
        if (mRestoreHandler != null) {
            mRestoreHandler.removeCallbacksAndMessages(null);
            mRestoreRunnable = null;
            mRestoreHandler = null;
        }

        /* disconnect camera when app exited */
        if (mDev.isProcessingImage()) {
            mDev.stopProcessImage();
            mVideoFragment.stopDrawingThread();
        }
        if (mDev.isLinked()) {
            mDev.dislinkCamera();
        }
        mDev = null;
        super.onDestroy();
    }

    private void play() {
        mDev.setColorPalette(MagDevice.ColorPalette.PaletteIronBow);
        if (mDev.startProcessImage(mVideoFragment, 0, 0)) {
            mVideoFragment.startDrawingThread(mDev);
        }
    }

    @Override
    public void linkResult(int result) {
        if (result == MagDevice.CONN_SUCC) {
            /* 连接成功 */
        } else if (result == MagDevice.CONN_FAIL) {
            /* 连接失败 */
        } else if (result == MagDevice.CONN_DETACHED) {
            /* 设备拔出*/
        }
        Log.i(TAG, "linkResult: "+result);
        updateButtons();
    }

    private class MagOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch(v.getId()) {
                case R.id.btnLink:
                    mDev.linkCamera(MainActivity.this, mSelectedDev.id, MainActivity.this);
                    updateButtons();
                    break;
                case R.id.btnPlay:
                    play();
                    updateButtons();
                    break;
                case R.id.btnOrigin://获得原始数据图像。
                   /* mDev.stopProcessImage();
                    mVideoFragment.stopDrawingThread();
                    updateButtons();*/
                    // bitmap= BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                    Origin();
                    //Current();


                    break;
                case R.id.btnCunrrent://获得当前视频流图像
                   /* mDev.dislinkCamera();
                    mVideoFragment.stopDrawingThread();
                    mDegree = 0;
                    updateButtons();*/
                  // Current();
                    Origin();

                    break;
                case R.id.btnFFC://获取FFC图像
                 /*   mDegree++;
                    if (mDegree > 3) {
                        mDegree = 0;
                    }
                    mDev.stopProcessImage();
                    mVideoFragment.stopDrawingThread();
                    mDev.setImageTransform(0, mDegree);
                    play();*/
                    int []temps=Origin();
                    //Current();
                    int []readeFfcs=FFC(temps);

                  /*   int []readeFfcs=FFCUtil.readFfc();*/
                  /*  for(int i=0;i<readeFfcs.length;i++){
                        readeFfcs[i]=readeFfcs[i]-10000;//还原真实差距
                    }*/
                    if(readeFfcs.length<10){
                        Toast.makeText(MainActivity.this, "请先校准FFC", Toast.LENGTH_SHORT).show();
                    }else {
                        int []afterFfcTemps= AfterFfc(temps,readeFfcs);//将原始数据通过FFc数据处理

                    }



                    break;
                case R.id.btnafter:
                    int []Orgintemps=Origin();
                    //Current();
                    int []readeFfc=FFCUtil.readFfc();

                    if(readeFfc.length<10){
                        Toast.makeText(MainActivity.this, "请先校准FFC", Toast.LENGTH_SHORT).show();
                    }else {
                      int []afterFfcTemps= AfterFfc(Orgintemps,readeFfc);//将原始数据通过FFc数据处理
                      int Orgintempsmaxmin[]= TempUtil.MaxMinTemp(Orgintemps);//找出原始数据的最大最小值
                      int afterFfcTempsmaxmin[]= TempUtil.MaxMinTemp(afterFfcTemps);//找出补偿后的最大最小值
                      Log.i(TAG, "Orgintempsmaxmin[0]:          "+Orgintempsmaxmin[0]);
                      Log.i(TAG, "afterFfcTempsmaxmin[0]:       "+afterFfcTempsmaxmin[0]);

                      /*测试自定义的在指定矩形区域寻找最大值*/
                      int []maxafterFfcTemps= TempUtil.DDNgetRectTemperatureInfo(afterFfcTemps,0,160,0,120);//获取指定矩形区域中最大的值
                      int []maxOrgintemps= TempUtil.DDNgetRectTemperatureInfo(Orgintemps,0,160,0,120);//获取指定矩形区域中最大的值

                      Log.i(TAG, "maxOrgintemps[0]:     "+maxOrgintemps[0]);
                      Log.i(TAG, "maxafterFfcTemps: "+maxafterFfcTemps[0]);
                    }




                    break;
            }
            mDevList.requestFocus();
        }
    }
/**
 * 根据已经得到的校准图，对原始数据进行校准
 * */
    private int[] AfterFfc(int[] orgintemps, int[] readeFfc) {//根据已经得到的校准图，对原始数据进行校准
        int []AfterTemps=new int[orgintemps.length];
        for(int i=0;i<AfterTemps.length;i++){
            AfterTemps[i]=orgintemps[i]-readeFfc[i];
        }


        final Bitmap bitmaps;
        bitmaps= TempUtil.CovertToBitMap(AfterTemps,0,100000);

        int maxmin[]= TempUtil.MaxMinTemp(AfterTemps);//找出最大最小值
        final int max=maxmin[0];
        final int min=maxmin[1];
        final int avg=maxmin[2];
        SaveTemps.saveIntTemps(AfterTemps,"After",maxmin);//保存FFC校准后的数据

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap1=bitmaps.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bitmap1);
                Paint paint = new Paint();
                paint.setTextSize(15);
                paint.setColor(Color.GREEN);
                canvas.drawText("Max: "+String.valueOf(max),20,20,paint);
                canvas.drawText("Min: "+String.valueOf(min),20,40,paint);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, baos);

                byte[] bytes=baos.toByteArray();
                Glide.with(MainActivity.this).load(bytes).into(iv_after);
            }
        });
        return AfterTemps;

    }
    /**
     * 从原始数据图中计算出FFC校准图
     * 获取到FFC
     * 原始数据，默认为拍摄一个均匀温度面
     * 算出平均值，并将每个店与平局值进行减
     * 并再减的基础上增加10度，也就是增加10000
     *
     * 并保存FFC得到的int[]
     * 以便之后的使用
     * */
    private int[] FFC(int[] temps) {//从原始数据图中计算出FFC校准图
        int []Ffctemp;
        Ffctemp=getFFC(temps);//获得FFC的校准图

        FFCTemps=Ffctemp;

        int maxmin1[]= TempUtil.MaxMinTemp(Ffctemp);//找出最大最小值
        SaveTemps.saveIntTemps(Ffctemp,"FFC",maxmin1);//保存FFC数据

        FFCUtil.saveIntFfc(Ffctemp);//保存校准图
/*显示校准图：*/
        final Bitmap bitmaps;
        //每个点加10000显示
        for(int i=0;i<Ffctemp.length;i++){
            Ffctemp[i]=Ffctemp[i]+10000;
        }
        bitmaps= TempUtil.CovertToBitMap(Ffctemp,0,100000);

        int maxmin[]= TempUtil.MaxMinTemp(Ffctemp);//找出最大最小值
        final int max=maxmin[0];
        final int min=maxmin[1];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap1=bitmaps.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bitmap1);
                Paint paint = new Paint();
                paint.setTextSize(15);
                paint.setColor(Color.GREEN);
                canvas.drawText("Max: "+String.valueOf(max),20,20,paint);
                canvas.drawText("Min: "+String.valueOf(min),20,40,paint);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, baos);

                byte[] bytes=baos.toByteArray();
                Glide.with(MainActivity.this).load(bytes).into(iv_FFC);
            }
        });

        return Ffctemp;

    }
    private int[]beforeTemps;
    private void Compare(int NowTemps[]) {//获得当前原始数据与上一张原始数据的差图
        if(beforeTemps!=null){

        final Bitmap bitmap;
        int compareTemps[]=new int[beforeTemps.length];
        for(int i=0;i<beforeTemps.length;i++){
            compareTemps[i]=NowTemps[i]-beforeTemps[i];
        }



        bitmap= TempUtil.CovertToBitMap(compareTemps,0,100);
        int maxmin[]= TempUtil.MaxMinTemp(compareTemps);//找出最大最小值
        final int max=maxmin[0];
        final int min=maxmin[1];

        SaveTemps.saveIntTemps(compareTemps,"Compare",maxmin);//保存比较后的数据

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap1=bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bitmap1);
                Paint paint = new Paint();
                paint.setTextSize(15);
                paint.setColor(Color.GREEN);
                canvas.drawText("Max: "+String.valueOf(max),20,20,paint);
                canvas.drawText("Min: "+String.valueOf(min),20,40,paint);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bytes=baos.toByteArray();
                Glide.with(MainActivity.this).load(bytes).into(iv_compare);
            }
        });

        }
    }


/**
 * 根据默认的方法
 * 获得当前图像
 * */
    private void Current() {//获得当前图像
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        File file = Environment.getExternalStorageDirectory();
        if (null == file) {
            return;
        }
        /*file = new File(file, "magnity/mx/media/");*/
        file = new File(file, "CurrentImg");
        if (!file.exists()) {
            file.mkdirs();
        }
        final String paths= System.currentTimeMillis() + ".bmp";
        //final String paths=  "Current.bmp";
        File files=new File(file,paths);
        if(files.exists()){
            files.delete();
            Log.i("delete", "delete: ");
        }
        final String path=file.getAbsolutePath() + File.separator + paths;
        //保存图片
        if (mDev.saveBMP(0, path)) {
            Toast.makeText(MainActivity.this, file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
        final File finalFile = file;
        Runnable runnable=new Runnable() {
            @Override
            public void run() {
                File fileIv = new File(finalFile, paths);
                Glide.with(MainActivity.this).load(fileIv).into(iv_compare);
            }
        };
        //加载图片
        myHander hander=new myHander();
        hander.postDelayed(runnable,200);
    }
   private class myHander extends Handler{
       @Override
       public void handleMessage(Message msg) {
           super.handleMessage(msg);

       }
   }



   /**
    * 根据
    * mDev.getTemperatureData(temps,false,false);
    * 获得原始图片
    * */
    public Paint SavePhotoPaint;
    private int[] Origin() {//获得原始图片
        int[] temps = new int[160*120];
        mDev.lock();
        mDev.getTemperatureData(temps,false,false);
        mDev.unlock();


        final Bitmap bitmap;
        Compare(temps);//与上一张进行比较
        beforeTemps=temps;//缓存上一张。



        bitmap= TempUtil.CovertToBitMap(temps,0,100);
        int maxmin[]= TempUtil.MaxMinTemp(temps);//找出最大最小值
        final int max=maxmin[0];
        final int min=maxmin[1];
        final int avg=maxmin[2];

        int []showTemps=temps;

        showTemps=TempUtil.ReLoad(showTemps);//旋转原始数据，x，y都旋转
        final int []maxTemp=TempUtil.DDNgetRectTemperatureInfo(showTemps,0,m_FrameWidth,0,m_FrameHeight);//获取指定矩形区域中最大的值

        if(SavePhotoPaint==null){
            SavePhotoPaint=new Paint();
            SavePhotoPaint.setStyle(Paint.Style.FILL);
            SavePhotoPaint.setStrokeWidth(1f);
            SavePhotoPaint.setTextSize(10f);
            SavePhotoPaint.setColor(Color.GREEN);
            SavePhotoPaint.setStrokeCap(Paint.Cap.ROUND);
        }
        SaveTemps.saveIntTemps(temps,"Origin",maxmin);//保存原始数据

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap1=bitmap.copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(bitmap1);
                Paint paint = new Paint();
                paint.setTextSize(15);
                paint.setColor(Color.GREEN);
             /*   canvas.drawText("Max: "+String.valueOf(max),20,20,paint);
                canvas.drawText("Min: "+String.valueOf(min),20,40,paint);*/

                float x2=maxTemp[1];
                float y2=maxTemp[2];
                float xStart=x2-4f;
                float xStop=x2+4f;
                float yStart=y2-4f;
                float yStop=y2+4f;
                canvas.drawLine(xStart, y2, xStop, y2, SavePhotoPaint);
                canvas.drawLine(x2, yStart, x2, yStop, SavePhotoPaint);

                Rect rt2 = new Rect();
                Paint textPaint=SavePhotoPaint;
                textPaint.getTextBounds(String.valueOf(max), 0, String.valueOf(max).length(), rt2);
                int cx2 = rt2.width();
                int cy2 = rt2.height();
                final int pad2 = 6;
                x2 += pad2;
                y2 += cy2 + pad2;
                if (x2 > 120-cx2) {
                    x2 -= pad2 * 2 + cx2;
                }
                if (y2 >160) {
                    y2 -= pad2 * 2 + cy2 * 2;
                }
                canvas.drawText(String.valueOf(max), x2, y2, textPaint);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap1.compress(Bitmap.CompressFormat.PNG, 100, baos);
                byte[] bytes=baos.toByteArray();
                Glide.with(MainActivity.this).load(bytes).into(iv_origin);
            }
        });
        return temps;
    }
}
