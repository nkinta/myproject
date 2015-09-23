/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import com.nkinta_pu.camera_sbgc_controller.utils.SimpleLiveviewSlicer;
import com.nkinta_pu.camera_sbgc_controller.utils.SimpleLiveviewSlicer.Payload;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A SurfaceView based class to draw liveview frames serially.
 */
public class SimpleStreamSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = SimpleStreamSurfaceView.class.getSimpleName();

    private boolean mWhileFetching;

    private final BlockingQueue<byte[]> mJpegQueue = new ArrayBlockingQueue<byte[]>(2);

    private final boolean mInMutableAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;

    private Thread mDrawerThread;

    private int mPreviousWidth = 0;

    private int mPreviousHeight = 0;

    private final Paint mFramePaint;

    private StreamErrorListener mErrorListener;

    private int mViewType = NORMAL_VIEW;

    private static final int NORMAL_VIEW = 0;
    private static final int VR_FULL_VIEW = 1;
    private static final int VR_VIEW = 2;
    private static final int NONE_VIEW = 3;

    /**
     * Constructor
     * 
     * @param context
     */
    public SimpleStreamSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    /**
     * Constructor
     * 
     * @param context
     * @param attrs
     */
    public SimpleStreamSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    /**
     * Constructor
     * 
     * @param context
     * @param attrs
     * @param defStyle
     */
    public SimpleStreamSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        getHolder().addCallback(this);
        mFramePaint = new Paint();
        mFramePaint.setDither(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // do nothing.
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // do nothing.
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mWhileFetching = false;
    }

    /**
     * Start retrieving and drawing liveview frame data by new threads.
     * 
     * @return true if the starting is completed successfully, false otherwise.
     * @see SimpleLiveviewSurfaceView#bindRemoteApi(SimpleRemoteApi)
     */
    public boolean start(final String streamUrl, StreamErrorListener listener) {
        mErrorListener = listener;

        if (streamUrl == null) {
            Log.e(TAG, "start() streamUrl is null.");
            mWhileFetching = false;
            mErrorListener.onError(StreamErrorListener.StreamErrorReason.OPEN_ERROR);
            return false;
        }

        if (mWhileFetching) {
            Log.w(TAG, "start() already starting.");
            return false;
        }

        mWhileFetching = true;

        // A thread for retrieving liveview data from server.
        new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Starting retrieving streaming data from server.");
                SimpleLiveviewSlicer slicer = null;

                try {

                    // Create Slicer to open the stream and parse it.
                    slicer = new SimpleLiveviewSlicer();
                    slicer.open(streamUrl);

                    while (mWhileFetching) {
                        final Payload payload = slicer.nextPayload();
                        if (payload == null) { // never occurs
                            Log.e(TAG, "Liveview Payload is null.");
                            continue;
                        }

                        if (mJpegQueue.size() == 2) {
                            mJpegQueue.remove();
                        }
                        mJpegQueue.add(payload.jpegData);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "IOException while fetching: " + e.getMessage());
                    mErrorListener.onError(StreamErrorListener.StreamErrorReason.IO_EXCEPTION);
                } finally {
                    if (slicer != null) {
                        slicer.close();
                    }

                    if (mDrawerThread != null) {
                        mDrawerThread.interrupt();
                    }

                    mJpegQueue.clear();
                    mWhileFetching = false;
                }
            }
        }.start();

        // A thread for drawing liveview frame fetched by above thread.
        mDrawerThread = new Thread() {
            @Override
            public void run() {
                Log.d(TAG, "Starting drawing stream frame.");
                Bitmap frameBitmap = null;

                BitmapFactory.Options factoryOptions = new BitmapFactory.Options();
                factoryOptions.inSampleSize = 1;
                if (mInMutableAvailable) {
                    initInBitmap(factoryOptions);
                }

                while (mWhileFetching) {
                    try {
                        byte[] jpegData = mJpegQueue.take();
                        frameBitmap = BitmapFactory.decodeByteArray(//
                                jpegData, 0, jpegData.length, factoryOptions);
                    } catch (IllegalArgumentException e) {
                        if (mInMutableAvailable) {
                            clearInBitmap(factoryOptions);
                        }
                        continue;
                    } catch (InterruptedException e) {
                        Log.i(TAG, "Drawer thread is Interrupted.");
                        break;
                    }

                    if (mInMutableAvailable) {
                        setInBitmap(factoryOptions, frameBitmap);
                    }
                    drawFrame(frameBitmap);
                }

                if (frameBitmap != null) {
                    frameBitmap.recycle();
                }
                mWhileFetching = false;
            }
        };
        mDrawerThread.start();
        return true;
    }

    /**
     * Request to stop retrieving and drawing liveview data.
     */
    public void stop() {
        mWhileFetching = false;
    }

    /**
     * Check to see whether start() is already called.
     * 
     * @return true if start() is already called, false otherwise.
     */
    public boolean isStarted() {
        return mWhileFetching;
    }

    public void shiftViewType() {
        mViewType = (mViewType + 1) % NONE_VIEW;
    }

    public void setViewType(int v) {
        mViewType = v;
    }

    public int getViewType() {
        return mViewType;
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initInBitmap(BitmapFactory.Options options) {
        options.inBitmap = null;
        options.inMutable = true;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void clearInBitmap(BitmapFactory.Options options) {
        if (options.inBitmap != null) {
            options.inBitmap.recycle();
            options.inBitmap = null;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setInBitmap(BitmapFactory.Options options, Bitmap bitmap) {
        options.inBitmap = bitmap;
    }

    /**
     * Draw frame bitmap onto a canvas.
     * 
     * @param frame
     */
    private void drawFrame(Bitmap frame) {
        if (frame.getWidth() != mPreviousWidth || frame.getHeight() != mPreviousHeight) {
            onDetectedFrameSizeChanged(frame.getWidth(), frame.getHeight());
            return;
        }
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }
        int w = frame.getWidth();
        int h = frame.getHeight();
        Rect src = new Rect(0, 0, w, h);

        if (mViewType == NORMAL_VIEW) {
            float by = Math.min((float) getWidth() / w, (float) getHeight() / h);
            int offsetX = (getWidth() - (int) (w * by)) / 2;
            int offsetY = (getHeight() - (int) (h * by)) / 2;
            Rect dst = new Rect(offsetX, offsetY, getWidth() - offsetX, getHeight() - offsetY);
            canvas.drawBitmap(frame, src, dst, mFramePaint);

        }
        else if (mViewType == VR_VIEW) {
            float wWidth = getWidth();
            float wHeight = getHeight();
            float by = Math.min(wWidth / (2 * w), wHeight / h);
            int offsetX = (int) ((wWidth - (2 * w * by)) / 2);
            int offsetY = (int) ((wHeight - (h * by)) / 2);

            Rect dstl = new Rect((int) offsetX, offsetY, (int) wWidth / 2, (int) wHeight - offsetY);
            canvas.drawBitmap(frame, src, dstl, mFramePaint);

            Rect dstr = new Rect((int) wWidth / 2, offsetY, (int) wWidth - offsetX, (int) wHeight - offsetY);
            canvas.drawBitmap(frame, src, dstr, mFramePaint);

        }
        else if (mViewType == VR_FULL_VIEW) {
            float wWidth = getWidth();
            float wHeight = getHeight();

            Rect dstl = new Rect(0, 0, (int) wWidth / 2, (int) wHeight);
            canvas.drawBitmap(frame, src, dstl, mFramePaint);

            Rect dstr = new Rect((int) wWidth / 2, 0, (int) wWidth, (int) wHeight);
            canvas.drawBitmap(frame, src, dstr, mFramePaint);

        }
        getHolder().unlockCanvasAndPost(canvas);

    }

    /**
     * Called when the width or height of liveview frame image is changed.
     * 
     * @param width
     * @param height
     */
    private void onDetectedFrameSizeChanged(int width, int height) {
        Log.d(TAG, "Change of aspect ratio detected");
        mPreviousWidth = width;
        mPreviousHeight = height;
        drawBlackFrame();
        drawBlackFrame();
        drawBlackFrame(); // delete triple buffers
    }

    /**
     * Draw black screen.
     */
    private void drawBlackFrame() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return;
        }

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawRect(new Rect(0, 0, getWidth(), getHeight()), paint);
        getHolder().unlockCanvasAndPost(canvas);
    }

    public interface StreamErrorListener {

        enum StreamErrorReason {
            IO_EXCEPTION,
            OPEN_ERROR,
        }

        void onError(StreamErrorReason reason);
    }
}
