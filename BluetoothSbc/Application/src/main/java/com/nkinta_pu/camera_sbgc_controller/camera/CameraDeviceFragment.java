/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.control.DeviceListActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
public class CameraDeviceFragment extends Fragment {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE = 3;

    private static final String TAG = CameraDeviceFragment.class.getSimpleName();

    private SimpleSsdpClient mSsdpClient;
    private boolean mActivityActive;

    private List<ServerDevice> mSeverDeviceList = new ArrayList<ServerDevice>() {};

    private WifiManager mWifiManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // setProgressBarIndeterminateVisibility(false);
        final MainActivity activity = (MainActivity)getActivity();
        // mListAdapter = new DeviceListAdapter(activity);
        mSsdpClient = new SimpleSsdpClient();

        Log.d(TAG, "onCreate() completed.");

        mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainActivity activity = (MainActivity)getActivity();
        switch (item.getItemId()) {
            case R.id.wifi_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(), WifiListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                return true;
            }
        }
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_camera_device, null);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final MainActivity activity = (MainActivity)getActivity();

        mActivityActive = true;
        /*
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
        */

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

        getView().findViewById(R.id.connect_wifi).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean connectFlag = false;
                if (connectFlag) {
                    Toast.makeText(activity, //
                            "connect wifi", //
                            Toast.LENGTH_SHORT).show(); //
                }
                else {
                    Toast.makeText(activity, //
                            "not connect wifi", //
                            Toast.LENGTH_SHORT).show(); //
                }
            }
        });
        TextView textWifiSsid = (TextView)  getView().findViewById(R.id.text_wifi_ssid);
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);


        // Show Wi-Fi SSID.
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            textWifiSsid.setText(Html.fromHtml(htmlLabel));
        } else {
            textWifiSsid.setText(R.string.msg_wifi_disconnect);
        }

        android.util.Log.d(TAG, "onResume() completed.");
    }

    /*
    private boolean connectWifi() {
        final MainActivity activity = (MainActivity)getActivity();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        List<ScanResult> apList = wifiManager.getScanResults();
        List<WifiConfiguration> confList = wifiManager.getConfiguredNetworks();
        String ssidPattern = "DIRECT-.*";
        WifiConfiguration targetConf = null;
        if (confList == null) {
            return false;
        }

        for (WifiConfiguration conf : confList) {
            boolean isExist = false;
            String ssid = conf.SSID.replace("\"", "");
            if (!ssid.matches(ssidPattern)) {
                continue;
            }

            for (ScanResult ap : apList) {
                if (!ssid.equals(ap.SSID)) {
                    continue;
                }
                isExist = true;
                break;
            }
            if (isExist == false) {
                continue;
            }

            wifiManager.enableNetwork(conf.networkId, true);
            targetConf = conf;
            break;
        }

        if (targetConf != null) {
            return wifiManager.enableNetwork(targetConf.networkId, true);
        }
        else {
            return false;
        }
    }

    */
    private void connectDevice(Intent data) {

        String ssidPattern = data.getExtras()
                .getString(WifiListActivity.DEVICE_SSID);

        List<WifiConfiguration> confList = mWifiManager.getConfiguredNetworks();
        WifiConfiguration targetConf = null;
        for (WifiConfiguration conf : confList) {
            String ssid = conf.SSID.replace("\"", "");
            if (!ssid.matches(ssidPattern)) {
                continue;
            }
            targetConf = conf;
            break;
        }

        if (targetConf != null) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            mWifiManager.enableNetwork(targetConf.networkId, true);
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                Toast.makeText(getActivity(), " interrupted",
                        Toast.LENGTH_SHORT).show();
            }

            for (int i = 0; i < 20; ++i) {
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
                if (wifiInfo.getSSID() == ssidPattern) {
                    break;
                }

                //Wait to connect
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Toast.makeText(getActivity(), " interrupted",
                            Toast.LENGTH_SHORT).show();
                }
            }
            getActivity().setProgressBarIndeterminateVisibility(true);

            searchDevices();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    // setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    com.example.android.common.logger.Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }
    private void searchDevices() {
        final MainActivity activity = (MainActivity)getActivity();
        final View view = getView();
        // mListAdapter.clearDevices();

        // final ServerDevice =

        activity.setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeverDeviceList.add(device);
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
                            if (mSeverDeviceList.isEmpty()) {
                                Toast.makeText(activity, //
                                        "cannot find", //
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }

                            connectToCamera(mSeverDeviceList.get(0));

                            /*
                            Toast.makeText(activity, //
                                    R.string.msg_device_search_finish, //
                                    Toast.LENGTH_SHORT).show();
                             */



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
    /*
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
    */

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
