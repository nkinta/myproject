package com.nkinta_pu.camera_sbgc_controller.control;

import android.app.Activity;
import android.os.Handler;

// import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by NK on 2015/08/23.
 */

public class HeadTrackHelper {

    private HeadTransform mHeadTransform;
    private HeadTracker mHeadTracker;

    private Runnable mRunnable;
    private Handler mHandler;

    private ArrayList<HeadTrackJob> mHeadTrackJobList;

    public static final int TIMING = 200;

    private float[] mFilterMultiple = null;

    private Deque<float[][]> mFilterValue = null;

    HeadTrackHelper(Activity activity) {
        mHeadTracker = HeadTracker.createFromContext(activity);
        mHeadTransform = new HeadTransform();
        mHeadTrackJobList = new ArrayList<HeadTrackJob>();

    }

    public void setJob(HeadTrackJob headTrackJob) {
        mHeadTrackJobList.add(headTrackJob);
    }


    public void createFilter(int power) {

        int size = power * 2 * 2 + 1;
        mFilterMultiple = new float[size];
        mFilterValue = new LinkedList<float[][]>();

        for (int i = 0; i < size; ++i) {
            int index = i - power * 2;
            double result = 1 / Math.sqrt(2 * Math.PI * power * power) * Math.exp(-index * index / (2 * power * power));
            mFilterMultiple[i] = (float)result;
        }
    }


    public void onStop() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        mHeadTracker.stopTracking();
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

        while (mFilterValue.size() > tempMultipleXyzValue.length) {
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
                xyzValue[j] += v[i][j];
            }
            ++i;
        }

        return tempXyzValue;

    }


    public void onStart() {
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // float[] tempFloat = new float[16];
                mHeadTracker.getLastHeadView(mHeadTransform.getHeadView(), 0);

                for (HeadTrackJob v: mHeadTrackJobList) {
                    v.doCommand(mHeadTransform);
                }
                mHandler.postDelayed(this, TIMING);
            }
        };

        mHandler = new Handler();
        mHandler.postDelayed(mRunnable, TIMING);
        mHeadTracker.startTracking();

    }
}
