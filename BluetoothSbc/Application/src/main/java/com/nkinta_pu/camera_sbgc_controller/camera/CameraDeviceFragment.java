/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.control.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
public class CameraDeviceFragment extends Fragment {

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE = 2;

    private static final String TAG = CameraDeviceFragment.class.getSimpleName();
    private static final int STATUS_MESSAGE_RESOURCE = R.id.wifi_status;

    private SimpleSsdpClient mSsdpClient;
    private boolean mActivityActive;

    private List<ServerDevice> mSeverDeviceList = new ArrayList<ServerDevice>() {};

    private WifiManager mWifiManager = null;
    private ProgressBar mProgressBar = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        // setProgressBarIndeterminateVisibility(false);
        final MainActivity activity = (MainActivity)getActivity();
        mSsdpClient = new SimpleSsdpClient();

        Log.d(TAG, "onCreate() completed.");

        mWifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        activity.registerReceiver(mReceiver, filter);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainActivity activity = (MainActivity)getActivity();
        switch (item.getItemId()) {
            case R.id.wifi_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan

                if (!mWifiManager.isWifiEnabled()) {
                    Toast.makeText(getActivity(), "Wi-Fi disconnected.",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }

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
        View view = inflater.inflate(R.layout.fragment_camera_connect, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mProgressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
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

        updateSsid();
    }

    private void updateSsid() {
        MainActivity activity = (MainActivity) getActivity();
        WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
        // Show Wi-Fi SSID.
        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // String htmlLabel = String.format("SSID: <b>%s</b>", wifiInfo.getSSID());
            setStatus(wifiInfo.getSSID());
        } else {
            setStatus(R.string.msg_wifi_disconnect);
        }

        android.util.Log.d(TAG, "onResume() completed.");
    }

    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect device");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    private synchronized void connectDevice(Intent data) {

        final String ssidPattern = data.getExtras()
                .getString(WifiListActivity.DEVICE_SSID);

        final String bssidPattern = data.getExtras()
                .getString(WifiListActivity.DEVICE_BSSID);

        final MainActivity activity = (MainActivity)getActivity();

        new Thread() {

            @Override
            public void run() {
                List<WifiConfiguration> confList = mWifiManager.getConfiguredNetworks();
                WifiConfiguration targetConf = null;
                for (WifiConfiguration conf : confList) {
                    String ssid = conf.SSID.trim().replace("\"", "");
                    if (!ssid.matches(ssidPattern)) {
                        continue;
                    }
                    targetConf = conf;
                    break;
                }
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setStatus(R.string.title_connecting);
                        activity.setProgressBarIndeterminateVisibility(true);
                    }
                });

                if (targetConf != null) {
                    // mProgressBar.setVisibility(View.GONE);
                    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

                    if (wifiInfo.getBSSID() == wifiInfo.getSSID()) {
                        searchDevices();
                    }
                    else {
                        boolean result = mWifiManager.enableNetwork(targetConf.networkId, true);
                        if (result) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    getActivity().setProgressBarIndeterminateVisibility(true);
                                }
                            });
                        }
                    }
                }
                else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "this isn't configured device.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                }

            };
        }.start();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data);
                }
                break;
            case REQUEST_ENABLE:
                if (resultCode == Activity.RESULT_OK) {
                    //
                }
        }
    }
    private void searchDevices() {
        final MainActivity activity = (MainActivity)getActivity();
        final View view = getView();

        activity.setProgressBarIndeterminateVisibility(true);
        mSsdpClient.search(new SimpleSsdpClient.SearchResultHandler() {

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search device found: " + device.getFriendlyName());
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        connectToCamera(device);
                    }
                });
            }

            @Override
            public void onTimeout() {
                // Called by non-UI thread.
                android.util.Log.d(TAG, ">> Search Timeout.");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        activity.setProgressBarIndeterminateVisibility(false);
                        if (mActivityActive) {
                            if (mSeverDeviceList.isEmpty()) {
                                Toast.makeText(activity, //
                                        "Search Timeout", //
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
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
                        if (mActivityActive) Toast.makeText(activity, //
                                R.string.msg_error_device_searching, //
                                Toast.LENGTH_SHORT).show(); //
                    }
                });
            }
        });
    }
    /**
     * @param id a string resource ID
     */
    private void setStatus(int id) {
        MainActivity activity = (MainActivity)getActivity();
        activity.setStatus(STATUS_MESSAGE_RESOURCE, id);
    }

    /**
     * @param status
     */
    private void setStatus(CharSequence status) {
        MainActivity activity = (MainActivity)getActivity();
        activity.setStatus(STATUS_MESSAGE_RESOURCE, status);
    }
    /**
     *
     * @param device
     */

    private void connectToCamera(ServerDevice device) {
        // Go to CameraSampleActivity.
        final MainActivity activity = (MainActivity)getActivity();
        Toast.makeText(activity, device.getFriendlyName(), Toast.LENGTH_SHORT).show();

        // Set target ServerDevice instance to control in Activity.
        SampleApplication app = (SampleApplication) activity.getApplication();
        SimpleRemoteApi remoteApi = app.getRemoteApi();
        if (remoteApi == null) {
            app.setRemoteApi(new SimpleRemoteApi(device));
        }

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

        Log.d(TAG, "onPause() completed.");
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info =  intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    Log.d(TAG, info.getDetailedState().name() + info.getTypeName() + info.getSubtypeName());
                    if (info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        String bssid =  intent.getStringExtra(WifiManager.EXTRA_BSSID);
                        WifiInfo wifiInfo =  intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                        wifiInfo.getSSID();
                        if (mProgressBar != null) {
                            mProgressBar.setVisibility(View.GONE);
                            Toast.makeText(getActivity(), "Search device", Toast.LENGTH_SHORT).show();
                        }
                        updateSsid();
                        searchDevices();
                    }
                }
                else {
                    Log.d(TAG, "null state");
                }
            }
        }
    };
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

/*
for (int i = 0; i < TRY_COUNT; ++i) {
    try {
        Thread.sleep(TRY_INTERVAL);
    }
    catch (InterruptedException e) {
        Log.d(TAG, e.getMessage());
    }

    WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
    String ssid = targetConf.SSID.trim().replace("\"", "");

    if (wifiInfo.getSSID().trim().equals(ssid)) {
        continue;
    }

    ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = null;
    if (connectivityManager != null) {
        networkInfo = connectivityManager.getActiveNetworkInfo();
    }

    if (networkInfo != null && networkInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
        successFlag = true;
        break;
    }

}
*/
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
        IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        activity.registerReceiver(mReceiver, filter);
        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        activity.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        activity.registerReceiver(mReceiver, filter);

            private void updateSsid() {
        TextView textWifiSsid = (TextView)  getView().findViewById(R.id.text_wifi_ssid);
        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

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


    */