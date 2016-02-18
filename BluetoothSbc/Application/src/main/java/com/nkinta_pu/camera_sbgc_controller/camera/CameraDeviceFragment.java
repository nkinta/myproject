/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Window;
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

    private static final int TRY_INTERVAL = 1000; // msec
    private static final int TRY_COUNT = 20;

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE = 2;

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

        IntentFilter filter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        activity.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        activity.registerReceiver(mReceiver, filter);
        filter = new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        activity.registerReceiver(mReceiver, filter);

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
                activity.setProgressBarIndeterminateVisibility(false);
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
                        activity.setProgressBarIndeterminateVisibility(true);
                    }
                });

                Boolean successFlag = false;

                if (targetConf != null) {
                    getActivity().setProgressBarIndeterminateVisibility(true);
                    mWifiManager.enableNetwork(targetConf.networkId, true);

                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "search device",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });

                    //
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

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
                com.example.android.common.logger.Log.d(TAG, "SUPPLICANT_CONNECTION_CHANGE_ACTION");
            } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(action)) {
                com.example.android.common.logger.Log.d(TAG, "SUPPLICANT_STATE_CHANGED_ACTION");
            } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                com.example.android.common.logger.Log.d(TAG, "NETWORK_STATE_CHANGED_ACTION");
                NetworkInfo info =  intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info != null) {
                    com.example.android.common.logger.Log.d(TAG, info.getDetailedState().name() + info.getTypeName() + info.getSubtypeName());
                    if (info.getDetailedState() == NetworkInfo.DetailedState.CONNECTED) {
                        String bssid =  intent.getStringExtra(WifiManager.EXTRA_BSSID);
                        WifiInfo wifiInfo =  intent.getParcelableExtra(WifiManager.EXTRA_WIFI_INFO);
                        wifiInfo.getSSID();

                        searchDevices();
                    }
                }
                else {
                    com.example.android.common.logger.Log.d(TAG, "null state");
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

    */