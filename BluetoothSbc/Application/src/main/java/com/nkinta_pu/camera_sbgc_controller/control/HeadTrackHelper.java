package com.nkinta_pu.camera_sbgc_controller.control;

import android.app.Activity;

// import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by NK on 2015/08/23.
 */

public class HeadTrackHelper {

    private HeadTransform mHeadTransform;
    private HeadTracker mHeadTracker;

    private float[] mFilterMultiple = null;

    private Deque<float[][]> mFilterValue = null;

    private float[] mLastAngle = null;

    private LooperManager mLooperManager = null;

    private float mRoll = 0.0f;

    HeadTrackHelper(Activity activity) {
        mHeadTracker = HeadTracker.createFromContext(activity);
        mHeadTransform = new HeadTransform();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);

                float[] angle = new float[3];
                mHeadTransform.getEulerAngles(mLastAngle, mRoll, angle, 0);
                float[] filteredAngle = setAndGetFilterValue(angle);
                mLastAngle = filteredAngle;
            }
        };

        mLooperManager = new LooperManager(runnable, "headtrack", 50);
    }

    static float[] normalize(float[] vList) {
        float[] resultList = new float[vList.length];
        float totalValue = 0;
        for (float v: vList) {
            totalValue += v;
        }

        for (int i = 0; i < vList.length; ++i) {
            resultList[i] = vList[i] / totalValue;
        }

        return resultList;
    }

    public void createFilter(int power) {

        int size = power * 2 * 2 + 1;
        float[] tempFilterMultiple = new float[size];
        mFilterValue = new LinkedList<float[][]>();

        for (int i = 0; i < size; ++i) {
            double index = i - power * 2;
            double result = 1 / Math.sqrt(2 * Math.PI * (float)power * (float)power) * Math.exp(-index * index / (2 * (float) power * (float)power));
            tempFilterMultiple[i] = (float)result;
        }

        mFilterMultiple = normalize(tempFilterMultiple);
    }

    public void setRoll(float value) {
        mLastAngle = null;
        mRoll = value;
    }

    public void reset() {
        mHeadTracker.resetTracker();
    }

    private void setFilterValue(float[] xyzValue) {

        float[][] tempMultipleXyzValue = new float[mFilterMultiple.length][];
        for (int i = 0; i < tempMultipleXyzValue.length; ++i) {
            float[] tempXyzValue = new float[ xyzValue.length];
            for (int j = 0; j < xyzValue.length; ++j) {
                tempXyzValue[j] = mFilterMultiple[i] * xyzValue[j];
            }
            tempMultipleXyzValue[i] = tempXyzValue;
        }

        while (mFilterValue.size() <= tempMultipleXyzValue.length) {
            mFilterValue.addFirst(tempMultipleXyzValue);
        }

        mFilterValue.removeLast();
    }

    private float[] setAndGetFilterValue(float[] xyzValue) {
        setFilterValue(xyzValue);
        float[] tempXyzValue = new float[xyzValue.length];
        int i = 0;
        for (float[][] v: mFilterValue) {
            for (int j = 0; j < xyzValue.length; ++j) {
                tempXyzValue[j] += v[i][j];
            }
            ++i;
        }

        return tempXyzValue;

    }

    public void onStop() {
        mHeadTracker.stopTracking();
        mLooperManager.stop();
    }

    public void onStart() {
        mHeadTracker.startTracking();
        createFilter(1);
        // float[] tempFloat = new float[16];
        mLooperManager.start();

    }

    public float[] getLastAngle() {
        return mLastAngle;
    }

}
