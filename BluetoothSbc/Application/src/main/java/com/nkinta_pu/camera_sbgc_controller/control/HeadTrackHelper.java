package com.nkinta_pu.camera_sbgc_controller.control;

import android.app.Activity;
import android.os.Handler;

// import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.sensors.HeadTracker;

import java.util.ArrayList;

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

    HeadTrackHelper(Activity activity) {
        mHeadTracker = HeadTracker.createFromContext(activity);
        mHeadTransform = new HeadTransform();
        mHeadTrackJobList = new ArrayList<HeadTrackJob>();

    }

    public void setJob(HeadTrackJob headTrackJob) {
        mHeadTrackJobList.add(headTrackJob);
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
