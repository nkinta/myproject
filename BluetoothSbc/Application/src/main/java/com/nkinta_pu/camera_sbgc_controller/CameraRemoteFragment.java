/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * An Activity class of Device Discovery screen.
 */
public class CameraRemoteFragment extends Fragment {

    private static final String TAG = CameraRemoteFragment.class.getSimpleName();

    private SimpleSsdpClient mSsdpClient;
    private boolean mActivityActive;

    private DeviceListAdapter mListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // setProgressBarIndeterminateVisibility(false);
        final MainActivity activity = (MainActivity)getActivity();
        mListAdapter = new DeviceListAdapter(activity);
        mSsdpClient = new SimpleSsdpClient();

        Log.d(TAG, "onCreate() completed.");
    }




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_device_discovery, null);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity activity = (MainActivity)getActivity();

        mActivityActive = true;
        ListView listView = (ListView) getView().findViewById(R.id.list_device);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ServerDevice device = (ServerDevice) listView.getAdapter().getItem(position);
                connectToCamera(device);
            }
        });

        getView().findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                if (!mSsdpClient.isSearching()) {
                    searchDevices();
                    btn.setEnabled(false);
                }
            }
        });

        // Show Wi-Fi SSID.
        TextView textWifiSsid = (TextView)  getView().findViewById(R.id.text_wifi_ssid);
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            textWifiSsid.setText(Html.fromHtml(htmlLabel));
        } else {
            textWifiSsid.setText(R.string.msg_wifi_disconnect);
        }

        android.util.Log.d(TAG, "onResume() completed.");
    }

    private void searchDevices() {
        final MainActivity activity = (MainActivity)getActivity();
        final View view = getView();
        mListAdapter.clearDevices();
        activity.setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.addDevice(device);
                    }
                });
            }

            @Override
            public void onFinished() {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search finished.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.setProgressBarIndeterminateVisibility(false);
                        view.findViewById(R.id.button_search).setEnabled(true);
                        if (mActivityActive) {
                            Toast.makeText(activity, //
                                    R.string.msg_device_search_finish, //
                                    Toast.LENGTH_SHORT).show(); //
                        }
                    }
                });
            }

            @Override
            public void onErrorFinished() {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search Error finished.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.setProgressBarIndeterminateVisibility(false);
                        view.findViewById(R.id.button_search).setEnabled(true);
                        if (mActivityActive) Toast.makeText(activity, //
                                R.string.msg_error_device_searching, //
                                Toast.LENGTH_SHORT).show(); //
                    }
                });
            }
        });
    }

    /**
     * Launch a CameraFragment.
     *
     * @param device
     */

    private void connectToCamera(ServerDevice device) {
        // Go to CameraSampleActivity.
        final MainActivity activity = (MainActivity)getActivity();
        Toast.makeText(activity, device.getFriendlyName(), Toast.LENGTH_SHORT).show();

        // Set target ServerDevice instance to control in Activity.
        SampleApplication app = (SampleApplication) activity.getApplication();
        app.setTargetServerDevice(device);
        // Intent intent = new Intent(this, CameraFragment.class);
        // startActivity(intent);
        activity.startCamera();
    }

    /**
     * Adapter class for DeviceList
     */
    @Override
    public void onPause() {
        super.onPause();
        mActivityActive = false;
        if (mSsdpClient != null && mSsdpClient.isSearching()) {
            mSsdpClient.cancelSearching();
        }

        android.util.Log.d(TAG, "onPause() completed.");
    }
}
