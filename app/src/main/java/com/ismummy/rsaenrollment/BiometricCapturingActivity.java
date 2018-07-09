package com.ismummy.rsaenrollment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.ismummy.rsaenrollment.HEROFUN.HAPI;
import com.ismummy.rsaenrollment.HEROFUN.LAPI;
import com.ismummy.rsaenrollment.bases.BaseActivity;

import butterknife.BindView;
import butterknife.OnClick;

public class BiometricCapturingActivity extends BaseActivity {

    @BindView(R.id.right_thumb_iv)
    ImageView finger;

    //for LAPI
    private LAPI m_cLAPI = null;
    private int m_hDevice = 0;
    private int[] RGBbits = new int[256 * 360];

    public static final int MESSAGE_SET_ID = 100;
    public static final int MESSAGE_SHOW_TEXT = 101;
    public static final int MESSAGE_SHOW_IMAGE = 200;
    public static final int MESSAGE_SHOW_BITMAP = 303;

    private HAPI m_cHAPI = null;
    private volatile boolean bContinue = false;

    private Context mContext;
    private ScreenBroadcastReceiver mScreenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biometric_capturing);

        m_cLAPI = new LAPI(this);
        m_cHAPI = new HAPI(this, m_fpsdkHandle);

        mContext = this;
        mScreenReceiver = new ScreenBroadcastReceiver();
        registerListener();
    }

    @OnClick(R.id.right_thumb_layout)
    void rightThumbClick() {
        if (bContinue) {
            m_cHAPI.DoCancel();
            bContinue = false;
            //btnEnroll.setText(String.format("Enroll"));
            return;
        }
        bContinue = true;
        Runnable r = new Runnable() {
            public void run() {
                FINGER_ENROLL("right_thumb");
            }
        };
        Thread s = new Thread(r);
        s.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OPEN_DEVICE();
    }

    @Override
    protected void onPause() {
        m_cHAPI.DoCancel();
        bContinue = false;
        CLOSE_DEVICE();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //bContinue = false;
        if (m_hDevice != 0) CLOSE_DEVICE();
        super.onDestroy();
        //android.os.Process.killProcess(android.os.Process.myPid());
    }

    private void registerListener() {
        if (mContext != null) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            mContext.registerReceiver(mScreenReceiver, filter);
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                onDestroy();
                //finish();
                //android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    }


    protected void OPEN_DEVICE() {
        String msg;
        m_hDevice = m_cLAPI.OpenDeviceEx();
        if (m_hDevice == 0) msg = "Can't open device !";
        else {
            msg = "OpenDevice() = OK";
        }
        m_cHAPI.m_hDev = m_hDevice;
        m_fEvent.sendMessage(m_fEvent.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, msg));
    }

    protected void CLOSE_DEVICE() {
        String msg;
        m_cHAPI.DoCancel();
        if (m_hDevice != 0) {
            //m_cLAPI.CtrlLed(m_hDevice, 0);	//for Optical
            m_cLAPI.CloseDeviceEx(m_hDevice);
        }
        msg = "CloseDevice() = OK";
        m_hDevice = 0;
        m_cHAPI.m_hDev = m_hDevice;
        m_fEvent.sendMessage(m_fEvent.obtainMessage(MESSAGE_SHOW_TEXT, 0, 0, msg));
    }

    protected void FINGER_ENROLL(String regid) {

        String msg = "";
        Resources res = getResources();
        if ((regid == null) || regid.isEmpty()) {
            bContinue = false;
            return;
        }

        boolean ret = m_cHAPI.Enroll(regid, true);
        if (ret) {
            msg = String.format("Enroll OK (ID=%s)", regid);
            m_cHAPI.DBRefresh();
        } else {
            msg = String.format("Enroll : False : %s", errorMessage(m_cHAPI.GetErrorCode()));
        }
        bContinue = false;
        m_fpsdkHandle.obtainMessage(HAPI.MSG_SHOW_TEXT, 0, 0, msg).sendToTarget();
    }

    private final Handler m_fEvent = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SHOW_TEXT:
                    toast((String) msg.obj);
                    break;
                case MESSAGE_SHOW_IMAGE:
                    ShowFingerBitmap((byte[]) msg.obj, msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    private final Handler m_appHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_SET_ID:
                    toast((String) msg.obj);
                    break;
                case MESSAGE_SHOW_TEXT:
                    toast((String) msg.obj);
                    break;
                case MESSAGE_SHOW_IMAGE:
                    ShowFingerBitmap((byte[]) msg.obj, msg.arg1, msg.arg2);
                    break;
                case MESSAGE_SHOW_BITMAP:
                    finger.setImageBitmap((Bitmap) msg.obj);
                    break;
            }
        }
    };

    private final Handler m_fpsdkHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String str = "";
            Resources res;
            switch (msg.what) {
                case 0xff:
                    break;
                case HAPI.MSG_SHOW_TEXT:
                    toast((String) msg.obj);
                    break;
                case HAPI.MSG_PUT_FINGER:
                    res = getResources();
                    str = res.getString(R.string.Put_your_finger);
                    if (msg.arg1 > 0) {
                        str += (" (" + String.valueOf(msg.arg1) + "/" + String.valueOf(msg.arg2) + ")");
                    }
                    str += " ! ";
                    str += (String) msg.obj;
                    toast(str);
                    break;
                case HAPI.MSG_FINGER_CAPTURED:
                    ShowFingerBitmap((byte[]) msg.obj, msg.arg1, msg.arg2);
                    break;
            }
        }
    };

    public String errorMessage(int errCode) {
        Resources res;
        res = getResources();
        switch (errCode) {
            case HAPI.ERROR_NONE:
                return res.getString(R.string.ERROR_NONE);
            case HAPI.ERROR_ARGUMENTS:
                return res.getString(R.string.ERROR_ARGUMENTS);
            case HAPI.ERROR_LOW_QUALITY:
                return res.getString(R.string.ERROR_LOW_QUALITY);
            case HAPI.ERROR_NEG_ACCESS:
                return res.getString(R.string.ERROR_NEG_ACCESS);
            case HAPI.ERROR_NEG_FIND:
                return res.getString(R.string.ERROR_NEG_FIND);
            case HAPI.ERROR_NEG_DELETE:
                return res.getString(R.string.ERROR_NEG_DELETE);
            case HAPI.ERROR_INITIALIZE:
                return res.getString(R.string.ERROR_INITIALIZE);
            case HAPI.ERROR_CANT_GENERATE:
                return res.getString(R.string.ERROR_CANT_GENERATE);
            case HAPI.ERROR_OVERFLOW_RECORD:
                return res.getString(R.string.ERROR_OVERFLOW_RECORD);
            case HAPI.ERROR_NEG_ADDNEW:
                return res.getString(R.string.ERROR_NEG_ADDNEW);
            case HAPI.ERROR_NEG_CLEAR:
                return res.getString(R.string.ERROR_NEG_CLEAR);
            case HAPI.ERROR_NONE_ACTIVITY:
                return res.getString(R.string.ERROR_NONE_ACTIVITY);
            case HAPI.ERROR_NONE_CAPIMAGE:
                return res.getString(R.string.ERROR_NONE_CAPIMAGE);
            case HAPI.ERROR_NOT_CALIBRATED:
                return res.getString(R.string.ERROR_NOT_CALIBRATED);
            case HAPI.ERROR_NONE_DEVICE:
                return res.getString(R.string.ERROR_NONE_DEVICE);
            case HAPI.ERROR_TIMEOUT_OVER:
                return res.getString(R.string.ERROR_TIMEOUT_OVER);
            case HAPI.ERROR_DO_CANCELED:
                return res.getString(R.string.ERROR_DOCANCELED);
            case HAPI.ERROR_EMPTY_DADABASE:
                return res.getString(R.string.ERROR_EMPTY_DADABASE);
            default:
                return String.format("errCode=%d", errCode);
        }
    }

    private void ShowFingerBitmap(byte[] image, int width, int height) {
        if (width == 0) return;
        if (height == 0) return;
        for (int i = 0; i < width * height; i++) {
            int v;
            if (image != null) v = image[i] & 0xff;
            else v = 0;
            RGBbits[i] = Color.rgb(v, v, v);
        }
        Bitmap bmp = Bitmap.createBitmap(RGBbits, width, height, Bitmap.Config.RGB_565);
        finger.setImageBitmap(bmp);
    }
}
