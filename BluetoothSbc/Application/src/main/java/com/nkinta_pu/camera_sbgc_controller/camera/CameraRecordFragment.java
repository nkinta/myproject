/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.camera.utils.DisplayHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Activity class of Sample Camera screen.
 */
public class CameraRecordFragment extends Fragment {

    private static final String TAG = CameraRecordFragment.class.getSimpleName();

    private ImageView mImagePictureWipe;

    private Spinner mSpinnerShootMode;

    private Button mButtonTakePicture;

    private Button mButtonRecStartStop;

    private Button mButtonZoomIn;

    private Button mButtonZoomOut;

    private Button mButtonContentsListMode;

    private TextView mTextCameraStatus;

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

        // activity.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // activity.setContentView(R.layout.fragment_camera);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_camera_record, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImagePictureWipe = (ImageView) view.findViewById(R.id.image_picture_wipe);

        // Spinner shootMode = (Spinner) getView().findViewById(R.id.spinner_shoot_mode);
        mSpinnerShootMode = (Spinner) view.findViewById(R.id.spinner_shoot_mode);

        mButtonTakePicture = (Button) view.findViewById(R.id.button_take_picture);
        mButtonRecStartStop = (Button) view.findViewById(R.id.button_rec_start_stop);
        mButtonZoomIn = (Button) view.findViewById(R.id.button_zoom_in);
        mButtonZoomOut = (Button) view.findViewById(R.id.button_zoom_out);
        mButtonContentsListMode = (Button) view.findViewById(R.id.button_contents_list);
        mTextCameraStatus = (TextView) view.findViewById(R.id.text_camera_status);

        mSpinnerShootMode.setEnabled(false);

    }

    private void doSettingRemoteApi() {
        final FragmentActivity activity = getActivity();
        SampleApplication app = (SampleApplication) activity.getApplication();
        mRemoteApi = app.getRemoteApi();
        if (mRemoteApi != null) {
            connect();
        }
    }

    public void connect() {

        createEvent();

        createUi();

        prepareOpenConnection();

        Log.d(TAG, "connect() completed.");
    }

    private void createEvent() {
        final FragmentActivity activity = getActivity();

        mEventObserver = new SimpleCameraEventObserver(activity.getApplicationContext(), mRemoteApi);
        mEventListener = new SimpleCameraEventObserver.ChangeListenerTmpl() {

            @Override
            public void onShootModeChanged(String shootMode) {
                Log.d(TAG, "onShootModeChanged() called: " + shootMode);
                refreshUi();
            }

            @Override
            public void onCameraStatusChanged(String status) {
                Log.d(TAG, "onCameraStatusChanged() called: " + status);
                refreshUi();
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
                    if (isCameraApiAvailable("actZoom")) {
                        Log.d(TAG, "onApiListModified(): prepareActZoomButtons()");
                        prepareActZoomButtons(true);
                    } else {
                        prepareActZoomButtons(false);
                    }
                }
            }

            @Override
            public void onZoomPositionChanged(int zoomPosition) {
                Log.d(TAG, "onZoomPositionChanged() called = " + zoomPosition);
                if (zoomPosition == 0) {
                    mButtonZoomIn.setEnabled(true);
                    mButtonZoomOut.setEnabled(false);
                } else if (zoomPosition == 100) {
                    mButtonZoomIn.setEnabled(false);
                    mButtonZoomOut.setEnabled(true);
                } else {
                    mButtonZoomIn.setEnabled(true);
                    mButtonZoomOut.setEnabled(true);
                }
            }

            @Override
            public void onLiveviewStatusChanged(boolean status) {
                Log.d(TAG, "onLiveviewStatusChanged() called = " + status);
            }

            @Override
            public void onStorageIdChanged(String storageId) {
                Log.d(TAG, "onStorageIdChanged() called: " + storageId);
                refreshUi();
            }
        };

        mEventObserver.activate();
    }

    private void createUi() {
        final FragmentActivity activity = getActivity();

        mLiveviewSurface = (SimpleStreamSurfaceView) activity.findViewById(R.id.surfaceview_liveview);
        mSpinnerShootMode.setFocusable(false);
        mButtonContentsListMode.setEnabled(false);

        mLiveviewSurface.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final MainActivity activity = (MainActivity) getActivity();

                // Set target ServerDevice instance to control in Activity.
                Intent intent = new Intent(activity, CameraRecordActivity.class);
                intent.putExtra("viewType", mLiveviewSurface.getViewType());
                startActivity(intent);
                return true;
            }
        });

        mLiveviewSurface.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mLiveviewSurface.shiftViewType();
            }
        });

        mButtonTakePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takeAndFetchPicture();
            }
        });
        mButtonRecStartStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if ("MovieRecording".equals(mEventObserver.getCameraStatus())) {
                    stopMovieRec();
                } else if ("IDLE".equals(mEventObserver.getCameraStatus())) {
                    startMovieRec();
                }
            }
        });

        mImagePictureWipe.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mImagePictureWipe.setVisibility(View.INVISIBLE);
            }
        });

        mButtonZoomIn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                actZoom("in", "1shot");
            }
        });

        mButtonZoomOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                actZoom("out", "1shot");
            }
        });

        mButtonZoomIn.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                actZoom("in", "start");
                return true;
            }
        });

        mButtonZoomOut.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                actZoom("out", "start");
                return true;
            }
        });

        mButtonZoomIn.setOnTouchListener(new View.OnTouchListener() {

            private long downTime = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - downTime > 500) {
                        actZoom("in", "stop");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downTime = System.currentTimeMillis();
                }
                return false;
            }
        });

        mButtonZoomOut.setOnTouchListener(new View.OnTouchListener() {

            private long downTime = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (System.currentTimeMillis() - downTime > 500) {
                        actZoom("out", "stop");
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downTime = System.currentTimeMillis();
                }
                return false;
            }
        });

        mButtonContentsListMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "Clicked contents list mode button");
                // prepareToStartContentsListMode();
            }
        });

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public void onResume() {
        super.onResume();

        doSettingRemoteApi();

        Log.d(TAG, "onResume() completed.");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRemoteApi == null) {
            return;
        }

        closeConnection();

        Log.d(TAG, "onPause() completed.");
    }

    public void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");
        final FragmentActivity activity = getActivity();
        activity.setProgressBarIndeterminateVisibility(true);

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

                    SampleApplication app = (SampleApplication) activity.getApplication();
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
                    DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(activity, false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(activity, false);
                }
            }
        }.start();
    }

    private static boolean isShootingStatus(String currentStatus) {
        Set<String> shootingStatus = new HashSet<String>();
        shootingStatus.add("IDLE");
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
        final FragmentActivity activity = getActivity();
        activity.runOnUiThread(new Runnable() {

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
                                refreshUi();
                            }

                            @Override
                            public void onShootModeChanged(String shootMode) {
                                refreshUi();
                            }

                            @Override
                            public void onStorageIdChanged(String storageId) {
                                refreshUi();
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
        final FragmentActivity activity = getActivity();
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
                            DisplayHelper.toast(activity.getApplicationContext(), //
                                    R.string.msg_error_non_supported_device);
                            activity.finish();
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

                    // prepare UIs
                    if (isCameraApiAvailable("getAvailableShootMode")) {
                        Log.d(TAG, "openConnection(): prepareShootModeSpinner()");
                        prepareShootModeSpinner();
                        // Note: hide progress bar on title after this calling.
                    }

                    // prepare UIs
                    if (isCameraApiAvailable("actZoom")) {
                        Log.d(TAG, "openConnection(): prepareActZoomButtons()");
                        prepareActZoomButtons(true);
                    } else {
                        prepareActZoomButtons(false);
                    }

                    Log.d(TAG, "openConnection(): completed.");
                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    DisplayHelper.setProgressIndicator(activity, false);
                    DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_error_connection);
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
     * Refresh UI appearance along with current "cameraStatus" and "shootMode".
     */
    private void refreshUi() {

        String cameraStatus = mEventObserver.getCameraStatus();
        String shootMode = mEventObserver.getShootMode();

        // CameraStatus TextView
        mTextCameraStatus.setText(cameraStatus);

        // Recording Start/Stop Button
        if ("MovieRecording".equals(cameraStatus)) {
            mButtonRecStartStop.setEnabled(true);
            mButtonRecStartStop.setText(R.string.button_rec_stop);
        } else if ("IDLE".equals(cameraStatus) && "movie".equals(shootMode)) {
            mButtonRecStartStop.setEnabled(true);
            mButtonRecStartStop.setText(R.string.button_rec_start);
        } else {
            mButtonRecStartStop.setEnabled(false);
        }

        // Take picture Button
        if ("still".equals(shootMode) && "IDLE".equals(cameraStatus)) {
            mButtonTakePicture.setEnabled(true);
        } else {
            mButtonTakePicture.setEnabled(false);
        }

        // Picture wipe Image
        if (!"still".equals(shootMode)) {
            mImagePictureWipe.setVisibility(View.INVISIBLE);
        }

        // Shoot Mode Buttons
        if ("IDLE".equals(cameraStatus) || "MovieRecording".equals(cameraStatus)) {
            mSpinnerShootMode.setEnabled(true);
            selectionShootModeSpinner(mSpinnerShootMode, shootMode);
        } else {
            mSpinnerShootMode.setEnabled(false);
        }

        // Contents List Button
        if (isApiSupported("getContentList") //
                && isApiSupported("getSchemeList") //
                && isApiSupported("getSourceList")) {
            String storageId = mEventObserver.getStorageId();
            if (storageId == null) {
                Log.d(TAG, "not update ContentsList button ");
            } else if ("No Media".equals(storageId)) {
                mButtonContentsListMode.setEnabled(false);
            } else {
                mButtonContentsListMode.setEnabled(true);
            }
        }
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

    /**
     * Check if the shoot mode is supported in this application.
     *
     * @param mode
     * @return
     */
    private boolean isSupportedShootMode(String mode) {
        if ("still".equals(mode) || "movie".equals(mode)) {
            return true;
        }
        return false;
    }

    /**
     * Prepare for Spinner to select "shootMode" by user.
     */
    private void prepareShootModeSpinner() {
        final FragmentActivity activity = getActivity();
        new Thread() {

            @Override
            public void run() {
                Log.d(TAG, "prepareShootModeSpinner(): exec.");
                JSONObject replyJson = null;
                try {
                    replyJson = mRemoteApi.getAvailableShootMode();

                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    final String currentMode = resultsObj.getString(0);
                    JSONArray availableModesJson = resultsObj.getJSONArray(1);
                    final List<String> availableModes = new ArrayList<String>();

                    for (int i = 0; i < availableModesJson.length(); i++) {
                        String mode = availableModesJson.getString(i);
                        if (!isSupportedShootMode(mode)) {
                            mode = "";
                        }
                        availableModes.add(mode);
                    }
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            prepareShootModeSpinnerUi(//
                                    availableModes.toArray(new String[0]), currentMode);
                            // Hide progress indeterminately on title bar.
                            activity.setProgressBarIndeterminateVisibility(false);
                        }
                    });
                } catch (IOException e) {
                    Log.w(TAG, "prepareShootModeRadioButtons: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "prepareShootModeRadioButtons: JSON format error.");
                }
            };
        }.start();
    }

    /**
     * Selection for Spinner UI of Shoot Mode.
     *
     * @param spinner
     * @param mode
     */
    private void selectionShootModeSpinner(Spinner spinner, String mode) {
        if (!isSupportedShootMode(mode)) {
            mode = "";
        }
        @SuppressWarnings("unchecked")
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinner.getAdapter();
        if (adapter != null) {
            mSpinnerShootMode.setSelection(adapter.getPosition(mode));
        }
    }

    /**
     * Prepare for Spinner UI of Shoot Mode.
     *
     * @param availableShootModes
     * @param currentMode
     */
    private void prepareShootModeSpinnerUi(String[] availableShootModes, String currentMode) {
        final FragmentActivity activity = getActivity();
        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter(activity, //
                android.R.layout.simple_spinner_item, availableShootModes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerShootMode.setAdapter(adapter);
        mSpinnerShootMode.setPrompt(getString(R.string.prompt_shoot_mode));
        selectionShootModeSpinner(mSpinnerShootMode, currentMode);
        mSpinnerShootMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            // selected Spinner dropdown item
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                if (!spinner.isFocusable()) {
                    // ignored the first call, because shoot mode has not
                    // changed
                    spinner.setFocusable(true);
                } else {
                    String mode = spinner.getSelectedItem().toString();
                    String currentMode = mEventObserver.getShootMode();
                    if (mode.isEmpty()) {
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_no_supported_shootmode);
                        // now state that can not be changed
                        selectionShootModeSpinner(spinner, currentMode);
                    } else {
                        if ("IDLE".equals(mEventObserver.getCameraStatus()) //
                                && !mode.equals(currentMode)) {
                            setShootMode(mode);
                        } else {
                            // now state that can not be changed
                            selectionShootModeSpinner(spinner, currentMode);
                        }
                    }
                }
            }

            // not selected Spinner dropdown item
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /**
     * Prepare for Button to select "actZoom" by user.
     *
     * @param flag
     */
    private void prepareActZoomButtons(final boolean flag) {
        Log.d(TAG, "prepareActZoomButtons(): exec.");
        final FragmentActivity activity = getActivity();
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                prepareActZoomButtonsUi(flag);
            }
        });

    }

    /**
     * Prepare for ActZoom Button UI.
     *
     * @param flag
     */
    private void prepareActZoomButtonsUi(boolean flag) {
        if (flag) {
            mButtonZoomOut.setVisibility(View.VISIBLE);
            mButtonZoomIn.setVisibility(View.VISIBLE);
        } else {
            mButtonZoomOut.setVisibility(View.GONE);
            mButtonZoomIn.setVisibility(View.GONE);
        }
    }

    /**
     * Call setShootMode
     *
     * @param mode
     */
    private void setShootMode(final String mode) {
        final FragmentActivity activity = getActivity();
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.setShootMode(mode);
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        // Success, but no refresh UI at the point.
                        Log.v(TAG, "setShootMode: success.");
                    } else {
                        Log.w(TAG, "setShootMode: error: " + resultCode);
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "setShootMode: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "setShootMode: JSON format error.");
                }
            }
        }.start();
    }

    /**
     * Take a picture and retrieve the image data.
     */
    public void takeAndFetchPicture() {
        final FragmentActivity activity = getActivity();
        if (mLiveviewSurface == null || !mLiveviewSurface.isStarted()) {
            DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_error_take_picture);
            return;
        }

        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actTakePicture();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONArray imageUrlsObj = resultsObj.getJSONArray(0);
                    String postImageUrl = null;
                    if (1 <= imageUrlsObj.length()) {
                        postImageUrl = imageUrlsObj.getString(0);
                    }
                    if (postImageUrl == null) {
                        Log.w(TAG, "takeAndFetchPicture: post image URL is null.");
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_take_picture);
                        return;
                    }
                    // Show progress indicator
                    DisplayHelper.setProgressIndicator(activity, true);

                    URL url = new URL(postImageUrl);
                    InputStream istream = new BufferedInputStream(url.openStream());
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4; // irresponsible value
                    final Drawable pictureDrawable =
                            new BitmapDrawable(getResources(), //
                                    BitmapFactory.decodeStream(istream, null, options));
                    istream.close();
                    activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            mImagePictureWipe.setVisibility(View.VISIBLE);
                            mImagePictureWipe.setImageDrawable(pictureDrawable);
                        }
                    });

                } catch (IOException e) {
                    Log.w(TAG, "IOException while closing slicer: " + e.getMessage());
                    DisplayHelper.toast(activity.getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } catch (JSONException e) {
                    Log.w(TAG, "JSONException while closing slicer");
                    DisplayHelper.toast(activity.getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } finally {
                    DisplayHelper.setProgressIndicator(activity, false);
                }
            }
        }.start();
    }

    /**
     * Call startMovieRec
     */
    private void startMovieRec() {
        final FragmentActivity activity = getActivity();
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "startMovieRec: exec.");
                    JSONObject replyJson = mRemoteApi.startMovieRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_rec_start);
                    } else {
                        Log.w(TAG, "startMovieRec: error: " + resultCode);
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "startMovieRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "startMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    /**
     * Call stopMovieRec
     */
    private void stopMovieRec() {
        final FragmentActivity activity = getActivity();
        new Thread() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "stopMovieRec: exec.");
                    JSONObject replyJson = mRemoteApi.stopMovieRec();
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    String thumbnailUrl = resultsObj.getString(0);
                    if (thumbnailUrl != null) {
                        DisplayHelper.toast(activity.getApplicationContext(), R.string.msg_rec_stop);
                    } else {
                        Log.w(TAG, "stopMovieRec: error");
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "stopMovieRec: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "stopMovieRec: JSON format error.");
                }
            }
        }.start();
    }

    /**
     * Call actZoom
     *
     * @param direction
     * @param movement
     */
    private void actZoom(final String direction, final String movement) {
        final FragmentActivity activity = getActivity();
        new Thread() {

            @Override
            public void run() {
                try {
                    JSONObject replyJson = mRemoteApi.actZoom(direction, movement);
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    int resultCode = resultsObj.getInt(0);
                    if (resultCode == 0) {
                        // Success, but no refresh UI at the point.
                        Log.v(TAG, "actZoom: success");
                    } else {
                        Log.w(TAG, "actZoom: error: " + resultCode);
                        DisplayHelper.toast(activity.getApplicationContext(), //
                                R.string.msg_error_api_calling);
                    }
                } catch (IOException e) {
                    Log.w(TAG, "actZoom: IOException: " + e.getMessage());
                } catch (JSONException e) {
                    Log.w(TAG, "actZoom: JSON format error.");
                }
            }
        }.start();
    }

    private void startLiveview() {
        final FragmentActivity activity = getActivity();
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
                            activity.runOnUiThread(new Runnable() {

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
