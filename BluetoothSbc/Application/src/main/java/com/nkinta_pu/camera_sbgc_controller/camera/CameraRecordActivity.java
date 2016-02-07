/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.camera.utils.DisplayHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Activity class of Sample Camera screen.
 */
public class CameraRecordActivity extends Activity {

    private static final String TAG = CameraRecordActivity.class.getSimpleName();

    // private ServerDevice mTargetServer;

    private SimpleRemoteApi mRemoteApi;

    private SimpleStreamSurfaceView mLiveviewSurface;

    private SimpleCameraEventObserver mEventObserver;

    private SimpleCameraEventObserver.ChangeListener mEventListener;

    private final Set<String> mAvailableCameraApiSet = new HashSet<String>();

    private final Set<String> mSupportedApiSet = new HashSet<String>();

    // private boolean connectFlag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setHasOptionsMenu(true);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_camera_record);

        SampleApplication application = (SampleApplication)getApplication();
        HeadTrackHelper headTrackHelper = application.getHeadTrackHelper();
        if (headTrackHelper != null) {
            headTrackHelper.reset();
        }

        SampleApplication app = (SampleApplication) getApplication();
        mRemoteApi = app.getRemoteApi();
        connect();

    }

    /*
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {


    }
    */

    private boolean doSettingRemoteApi() {
        SampleApplication app = (SampleApplication) getApplication();
        final ServerDevice device = app.getTargetServerDevice();
        if (device == null) {
            return false;
        }
        if (mRemoteApi == null) {
            mRemoteApi = new SimpleRemoteApi(device);
            app.setRemoteApi(mRemoteApi);
            connect();
        }
        return true;
    }

    public void connect() {
        mEventObserver = new SimpleCameraEventObserver(getApplicationContext(), mRemoteApi);

        mEventListener = new SimpleCameraEventObserver.ChangeListenerTmpl() {

            @Override
            public void onShootModeChanged(String shootMode) {
                Log.d(TAG, "onShootModeChanged() called: " + shootMode);
            }

            @Override
            public void onCameraStatusChanged(String status) {
                Log.d(TAG, "onCameraStatusChanged() called: " + status);
            }

            @Override
            public void onApiListModified(List<String> apis) {
                Log.d(TAG, "onApiListModified() called");
                synchronized (mAvailableCameraApiSet) {
                    mAvailableCameraApiSet.clear();
                    for (String api : apis) {
                        mAvailableCameraApiSet.add(api);
                    }
                    if (!mEventObserver.getLiveviewStatus() //
                            && isCameraApiAvailable("startLiveview")) {
                        if (mLiveviewSurface != null && !mLiveviewSurface.isStarted()) {
                            startLiveview();
                        }
                    }
                }
            }

            @Override
            public void onZoomPositionChanged(int zoomPosition) {
                Log.d(TAG, "onZoomPositionChanged() called = " + zoomPosition);
            }

            @Override
            public void onLiveviewStatusChanged(boolean status) {
                Log.d(TAG, "onLiveviewStatusChanged() called = " + status);
            }

            @Override
            public void onStorageIdChanged(String storageId) {
                Log.d(TAG, "onStorageIdChanged() called: " + storageId);
            }
        };

        Log.d(TAG, "onCreate() completed.");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() completed.");
        mEventObserver.activate();

        mLiveviewSurface = (SimpleStreamSurfaceView) findViewById(R.id.surfaceview_liveview);
        int viewType = getIntent().getIntExtra("viewType", 0);
        mLiveviewSurface.setViewType(viewType);

        mLiveviewSurface.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                SampleApplication app = (SampleApplication)getApplication();
                HeadTrackHelper headTrackHelper = app.getHeadTrackHelper();
                headTrackHelper.reset();
            }
        });

        prepareOpenConnection();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRemoteApi == null) {
            return;
        }

        closeConnection();

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d(TAG, "onPause() completed.");
    }

    public void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");
        setProgressBarIndeterminateVisibility(true);

        new Thread() {

            @Override
            public void run() {
                try {
                    // Get supported API list (Camera API)
                    JSONObject replyJsonCamera = mRemoteApi.getCameraMethodTypes();
                    loadSupportedApiList(replyJsonCamera);

                    try {
                        // Get supported API list (AvContent API)
                        JSONObject replyJsonAvcontent = mRemoteApi.getAvcontentMethodTypes();
                        loadSupportedApiList(replyJsonAvcontent);
                    } catch (IOException e) {
                        Log.d(TAG, "AvContent is not support.");
                    }

                    SampleApplication app = (SampleApplication) getApplication();
                    app.setSupportedApiList(mSupportedApiSet);

                    if (!isApiSupported("setCameraFunction")) {

                        // this device does not support setCameraFunction.
                        // No need to check camera status.

                        openConnection();

                    } else {

                        // this device supports setCameraFunction.
                        // after confirmation of camera state, open connection.
                        Log.d(TAG, "this device support set camera function");

                        if (!isApiSupported("getEvent")) {
                            Log.e(TAG, "this device is not support getEvent");
                            openConnection();
                            return;
                        }

                        // confirm current camera status
                        String cameraStatus = null;
                        JSONObject replyJson = mRemoteApi.getEvent(false);
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                        String type = cameraStatusObj.getString("type");
                        if ("cameraStatus".equals(type)) {
                            cameraStatus = cameraStatusObj.getString("cameraStatus");
                        } else {
                            throw new IOException();
                        }

                        if (isShootingStatus(cameraStatus)) {
                            Log.d(TAG, "camera function is Remote Shooting.");
                            openConnection();
                        } else {
                            // set Listener
                            startOpenConnectionAfterChangeCameraState();

                            // set Camera function to Remote Shooting
                            replyJson = mRemoteApi.setCameraFunction("Remote Shooting");
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(CameraRecordActivity.this, false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(CameraRecordActivity.this, false);
                }
            }
        }.start();
    }

    private static boolean isShootingStatus(String currentStatus) {
        Set<String> shootingStatus = new HashSet<String>();
        shootingStatus.add("IDLE");
        shootingStatus.add("NotReady");
        shootingStatus.add("StillCapturing");
        shootingStatus.add("StillSaving");
        shootingStatus.add("MovieWaitRecStart");
        shootingStatus.add("MovieRecording");
        shootingStatus.add("MovieWaitRecStop");
        shootingStatus.add("MovieSaving");
        shootingStatus.add("IntervalWaitRecStart");
        shootingStatus.add("IntervalRecording");
        shootingStatus.add("IntervalWaitRecStop");
        shootingStatus.add("AudioWaitRecStart");
        shootingStatus.add("AudioRecording");
        shootingStatus.add("AudioWaitRecStop");
        shootingStatus.add("AudioSaving");

        return shootingStatus.contains(currentStatus);
    }

    private void startOpenConnectionAfterChangeCameraState() {
        Log.d(TAG, "startOpenConectiontAfterChangeCameraState() exec");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                mEventObserver
                        .setEventChangeListener(new SimpleCameraEventObserver.ChangeListenerTmpl() {

                            @Override
                            public void onCameraStatusChanged(String status) {
                                Log.d(TAG, "onCameraStatusChanged:" + status);
                                if ("IDLE".equals(status) || "NotReady".equals(status)) {
                                    openConnection();
                                }
                            }

                        });

                mEventObserver.start();
            }
        });
    }

    /**
     * Open connection to the camera device to start monitoring Camera events
     * and showing liveview.
     */
    private void openConnection() {
        mEventObserver.setEventChangeListener(mEventListener);
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "openConnection(): exec.");

                try {
                    JSONObject replyJson = null;

                    // getAvailableApiList
                    replyJson = mRemoteApi.getAvailableApiList();
                    loadAvailableCameraApiList(replyJson);

                    // check version of the server device
                    if (isCameraApiAvailable("getApplicationInfo")) {
                        Log.d(TAG, "openConnection(): getApplicationInfo()");
                        replyJson = mRemoteApi.getApplicationInfo();
                        if (!isSupportedServerVersion(replyJson)) {
                            DisplayHelper.toast(getApplicationContext(), //
                                    R.string.msg_error_non_supported_device);
                            finish();
                            return;
                        }
                    } else {
                        // never happens;
                        return;
                    }

                    // startRecMode if necessary.
                    if (isCameraApiAvailable("startRecMode")) {
                        Log.d(TAG, "openConnection(): startRecMode()");
                        replyJson = mRemoteApi.startRecMode();

                        // Call again.
                        replyJson = mRemoteApi.getAvailableApiList();
                        loadAvailableCameraApiList(replyJson);
                    }

                    // getEvent start
                    if (isCameraApiAvailable("getEvent")) {
                        Log.d(TAG, "openConnection(): EventObserver.start()");
                        mEventObserver.start();
                    }

                    // Liveview start
                    if (isCameraApiAvailable("startLiveview")) {
                        Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                        startLiveview();
                    }

                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    DisplayHelper.setProgressIndicator(CameraRecordActivity.this, false);
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_connection);
                }
            }
        }.start();

    }

    /**
     * Stop monitoring Camera events and close liveview connection.
     */
    private void closeConnection() {

        Log.d(TAG, "closeConnection(): exec.");
        // Liveview stop
        Log.d(TAG, "closeConnection(): LiveviewSurface.stop()");
        if (mLiveviewSurface != null) {
            mLiveviewSurface.stop();
            mLiveviewSurface = null;
            stopLiveview();
        }

        // getEvent stop
        Log.d(TAG, "closeConnection(): EventObserver.release()");
        mEventObserver.release();

        // stopRecMode if necessary.
        if (isCameraApiAvailable("stopRecMode")) {
            new Thread() {

                @Override
                public void run() {
                    Log.d(TAG, "closeConnection(): stopRecMode()");
                    try {
                        mRemoteApi.stopRecMode();
                    } catch (IOException e) {
                        Log.w(TAG, "closeConnection: IOException: " + e.getMessage());
                    }
                }
            }.start();
        }

        Log.d(TAG, "closeConnection(): completed.");
    }

    /**
     * Retrieve a list of APIs that are available at present.
     *
     * @param replyJson
     */
    private void loadAvailableCameraApiList(JSONObject replyJson) {
        synchronized (mAvailableCameraApiSet) {
            mAvailableCameraApiSet.clear();
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("result");
                JSONArray apiListJson = resultArrayJson.getJSONArray(0);
                for (int i = 0; i < apiListJson.length(); i++) {
                    mAvailableCameraApiSet.add(apiListJson.getString(i));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadAvailableCameraApiList: JSON format error.");
            }
        }
    }

    /**
     * Retrieve a list of APIs that are supported by the target device.
     *
     * @param replyJson
     */
    private void loadSupportedApiList(JSONObject replyJson) {
        synchronized (mSupportedApiSet) {
            try {
                JSONArray resultArrayJson = replyJson.getJSONArray("results");
                for (int i = 0; i < resultArrayJson.length(); i++) {
                    mSupportedApiSet.add(resultArrayJson.getJSONArray(i).getString(0));
                }
            } catch (JSONException e) {
                Log.w(TAG, "loadSupportedApiList: JSON format error.");
            }
        }
    }

    /**
     * Check if the specified API is available at present. This works correctly
     * only for Camera API.
     *
     * @param apiName
     * @return
     */
    private boolean isCameraApiAvailable(String apiName) {
        boolean isAvailable = false;
        synchronized (mAvailableCameraApiSet) {
            isAvailable = mAvailableCameraApiSet.contains(apiName);
        }
        return isAvailable;
    }

    /**
     * Check if the specified API is supported. This is for camera and avContent
     * service API. The result of this method does not change dynamically.
     *
     * @param apiName
     * @return
     */
    private boolean isApiSupported(String apiName) {
        boolean isAvailable = false;
        synchronized (mSupportedApiSet) {
            isAvailable = mSupportedApiSet.contains(apiName);
        }
        return isAvailable;
    }

    /**
     * Check if the version of the server is supported in this application.
     *
     * @param replyJson
     * @return
     */
    private boolean isSupportedServerVersion(JSONObject replyJson) {
        try {
            JSONArray resultArrayJson = replyJson.getJSONArray("result");
            String version = resultArrayJson.getString(1);
            String[] separated = version.split("\\.");
            int major = Integer.valueOf(separated[0]);
            if (2 <= major) {
                return true;
            }
        } catch (JSONException e) {
            Log.w(TAG, "isSupportedServerVersion: JSON format error.");
        } catch (NumberFormatException e) {
            Log.w(TAG, "isSupportedServerVersion: Number format error.");
        }
        return false;
    }

    private void startLiveview() {
        if (mLiveviewSurface == null) {
            Log.w(TAG, "startLiveview mLiveviewSurface is null.");
            return;
        }
        new Thread() {
            @Override
            public void run() {

                try {
                    JSONObject replyJson = null;
                    replyJson = mRemoteApi.startLiveview();

                    if (!SimpleRemoteApi.isErrorReply(replyJson)) {
                        JSONArray resultsObj = replyJson.getJSONArray("result");
                        if (1 <= resultsObj.length()) {
                            // Obtain liveview URL from the result.
                            final String liveviewUrl = resultsObj.getString(0);
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    mLiveviewSurface.start(liveviewUrl, //
                                            new SimpleStreamSurfaceView.StreamErrorListener() {

                                                @Override
                                                public void onError(StreamErrorReason reason) {
                                                    stopLiveview();
                                                }
                                            });
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startLiveview IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startLiveview JSONException: " + e.getMessage());
                }
            }
        }.start();
    }

    private void stopLiveview() {
        new Thread() {
            @Override
            public void run() {
                try {
                    mRemoteApi.stopLiveview();
                } catch (IOException e) {
                    Log.w(TAG, "stopLiveview IOException: " + e.getMessage());
                }
            }
        }.start();
    }

}
