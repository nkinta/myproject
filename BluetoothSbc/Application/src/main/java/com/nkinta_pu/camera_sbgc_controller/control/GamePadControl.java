package com.nkinta_pu.camera_sbgc_controller.control;

import android.util.FloatMath;
import android.view.MotionEvent;

import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

/**
 * Created by NK on 2016/04/06.
 */
public class GamePadControl {

    SimpleBgcControl mSimpleBgcControl = null;

    MainParameter.ControlGamePadParam mControlGamePadParam = null;

    void setSimpleBgcControl(SimpleBgcControl simpleBgcControl, MainParameter.ControlGamePadParam controlGamePadParam) {
        mSimpleBgcControl = simpleBgcControl;
        mControlGamePadParam = controlGamePadParam;
    }

    void dispatchGenericMotionEvent(MotionEvent e) {

        if (mGamePadJob == null) {
            return false;
        }
        // Use the hat axis value to find the D-pad direction
        MotionEvent motionEvent = (MotionEvent) e;
        float lxaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
        float lyaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

        float rxaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Z);
        float ryaxis = motionEvent.getAxisValue(MotionEvent.AXIS_RZ);

        mGamePadJob.doCommand(new float[] {lxaxis, lyaxis, rxaxis, ryaxis});

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

        final float roll = filter(v[mControlGamePadParam.mRollId.value], s, d, e);
        final float pitch = filter(v[mControlGamePadParam.mPitchId.value], s, d, e);
        final float yaw = filter(v[mControlGamePadParam.mYawId.value], s, d, e);

        /*
        outputValueTextView.setText(OUTPUT_VALUE_STRING + "roll pitch yaw -> "
                        + String.format("%3.2f", roll) + " - " + String.format("%3.2f", pitch) + String.format("%3.2f", yaw)
        );
        */

        new Thread() {
            @Override
            public void run() {
                mSimpleBgcControl.setSpeed(new float[]{roll, pitch, yaw});
            }
        }.run();

    }

}
