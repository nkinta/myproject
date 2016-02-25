package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Created by kanenao on 2015/11/05.
 */
public class LooperManager {

    private final Runnable mRunnable;

    // private Handler mHandler = null;

    private final String mThreadName;
    private HandlerThread mHandlerThread;

    private boolean mStatus;

    public final int mTiming;

    LooperManager(Runnable runnable, String threadName, int timing) {
        mRunnable = runnable;
        mThreadName = threadName;
        mTiming = timing;

        mHandlerThread = new HandlerThread(mThreadName);
        mHandlerThread.start();
        mStatus = false;
    }

    public void start() {


        mStatus = true;
        final Handler handler = new Handler(mHandlerThread.getLooper());
        Runnable looperRunnable = new Runnable() {
            @Override
            public void run() {
                if (!mStatus) {
                    return;
                }
                mRunnable.run();
                handler.postDelayed(this, mTiming);
            }
        };
        handler.post(looperRunnable);

        // Looper.prepare();
        // mHandler = new Handler();
        // Looper.loop();
        // mChatService.send(command.getCommandData());

    }

    public void stop() {
        mStatus = false;
    }

}
