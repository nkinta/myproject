package com.nkinta_pu.camera_sbgc_controller.control;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kanenao on 2015/09/18.
 */

public class CommandDispatcher {

    // Message types sent from the BluetoothService Handler
    class LoopThread extends Thread {
        private static final String TAG = "LoopThread";
        private boolean mContinue = true;
        @Override
        public void run() {
            while (mContinue) {
                try {
                    Runnable runnable = mRunnableBlockingQueue.take();
                    runnable.run();
                }
                catch (InterruptedException e) {
                    Log.e(TAG, "InterruptedException", e);
                }

            }
        };

        public void stopRunning() {
            mContinue = false;
        }
    };

    private final ArrayBlockingQueue<Runnable> mRunnableBlockingQueue = new ArrayBlockingQueue<Runnable>(1);

    private LoopThread mCommandThread = null;

    public void start() {
        mCommandThread = new LoopThread();
        mCommandThread.start();
    }

    public void stop() {
        if (mCommandThread != null) {
            mCommandThread.stopRunning();
        }
        mCommandThread = null;
    }

    public void setCommand(Runnable runnable) {
        mRunnableBlockingQueue.offer(runnable);
    }

    public boolean existNextCommand() {
        return mRunnableBlockingQueue.size() > 0;
    }

}
