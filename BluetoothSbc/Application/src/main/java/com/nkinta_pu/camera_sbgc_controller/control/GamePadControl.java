package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

/**
 * Created by NK on 2016/04/06.
 */
public class GamePadControl {

    public static final int UPDATE_PARAMETER = 1;
    public static final String LX_RX_LY_RY = "LX_RX_LY_RY";
    public static final String ROLL_PITCH_YAW_STRING = "ROLL_PITCH_YAW";

    SimpleBgcControl mSimpleBgcControl = null;

    MainParameter.ControlGamePadParam mControlGamePadParam = null;

    private Handler mHandler = null;

    public GamePadControl(MainParameter mainParameter) {
        mControlGamePadParam = mainParameter.mControlGamePadParam;
    }

    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setSimpleBgcControl(SimpleBgcControl simpleBgcControl) {
        mSimpleBgcControl = simpleBgcControl;
    }

    public void dispatchGenericMotionEvent(MotionEvent e) {

        // Use the hat axis value to find the D-pad direction
        MotionEvent motionEvent = (MotionEvent) e;
        float lxaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
        float lyaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

        float rxaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Z);
        float ryaxis = motionEvent.getAxisValue(MotionEvent.AXIS_RZ);

        doCommand(new float[] {lxaxis, lyaxis, rxaxis, ryaxis});

    }

    private static float filter(float v, float speed, float offset, float expo) {
        float pv = speed * FloatMath.pow(Math.max(0.0f, Math.abs(v) - offset), expo);
        float rv;
        if (v > 0) {
            rv = pv;
        }
        else {
            rv = -pv;
        }

        return rv;
    }

    private void  doCommand(final float[] v) {

        float rollMultipleValue = (mControlGamePadParam.mRollInverseFlag.value) ? -1.0f: 1.0f;
        float pitchMultipleValue = (mControlGamePadParam.mPitchInverseFlag.value) ? -1.0f: 1.0f;
        float yawMultipleValue = (mControlGamePadParam.mYawInverseFlag.value) ? -1.0f: 1.0f;

        float s = mControlGamePadParam.mSpeed.value;
        float d = mControlGamePadParam.mDeadBand.value;
        float e = mControlGamePadParam.mExpo.value;

        final float roll = filter(v[mControlGamePadParam.mRollId.value], s, d, e) * rollMultipleValue;
        final float pitch = filter(v[mControlGamePadParam.mPitchId.value], s, d, e) * pitchMultipleValue;
        final float yaw = filter(v[mControlGamePadParam.mYawId.value], s, d, e) * yawMultipleValue;

        /*
        outputValueTextView.setText(OUTPUT_VALUE_STRING + "roll pitch yaw -> "
                        + String.format("%3.2f", roll) + " - " + String.format("%3.2f", pitch) + String.format("%3.2f", yaw)
        );
        */
        if (mSimpleBgcControl != null) {
            new Thread() {
                @Override
                public void run() {
                    mSimpleBgcControl.setSpeed(new float[]{roll, pitch, yaw});
                }
            }.run();
        }

        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(UPDATE_PARAMETER);
            Bundle bundle = new Bundle();
            bundle.putFloatArray(ROLL_PITCH_YAW_STRING, new float[]{roll, pitch, yaw});
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }


    }

}
