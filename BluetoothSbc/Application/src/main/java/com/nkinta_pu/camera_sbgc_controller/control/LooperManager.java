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
    private HandlerThread mHandlerThread = null;

    public final int mTiming;

    LooperManager(Runnable runnable, String threadName, int timing) {
        mRunnable = runnable;
        mThreadName = threadName;
        mTiming = timing;

        mHandlerThread = new HandlerThread(mThreadName);

    }

    public void start() {

        mHandlerThread.start();

        final Handler handler = new Handler(mHandlerThread.getLooper());
        Runnable looperRunnable = new Runnable() {
            @Override
            public void run() {
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
        mHandlerThread.quit();

    }

}
