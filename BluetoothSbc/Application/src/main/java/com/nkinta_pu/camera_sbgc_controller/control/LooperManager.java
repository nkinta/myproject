package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

/**
 * Created by kanenao on 2015/11/05.
 */
public class LooperManager {

    private final Runnable mRunnable;

    private Handler mHandler;

    public final int mTiming;

    LooperManager(Runnable runnable, int timing) {
        mRunnable = runnable;
        mTiming = timing;
    }

    public void start() {
        new HandlerThread("looper") {
            // private Runnable mLooperRunnable = null;

            @Override
            public void run() {
                Looper.prepare();
                Runnable looperRunnable = new Runnable() {
                    @Override
                    public void run() {
                        LooperManager:mRunnable.run();
                        mHandler.postDelayed(this, mTiming);
                    }
                };
                mHandler = new Handler();
                Looper.loop();
                mHandler.postDelayed(looperRunnable, mTiming);
                // mChatService.send(command.getCommandData());
            }

        }.start();

    }

    public void stop() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

    }

}
