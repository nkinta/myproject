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

    // private Handler mHandlerThread;

    public final int mTiming;

    LooperManager(Runnable runnable, int timing) {
        mRunnable = runnable;
        mTiming = timing;
    }

    public void start() {
        HandlerThread handlerThread = new HandlerThread("looper");
        handlerThread.start();

        final Handler handler = new Handler(handlerThread.getLooper());
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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }

    }

}
