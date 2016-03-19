package com.nkinta_pu.camera_sbgc_controller.camera;

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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.control.Constants;

import java.io.IOException;
import java.net.SocketException;
import java.util.List;

/**
 * Created by NK on 2016/03/12.
 */
public class WifiService {

    private static final String TAG = "WifiService";

    private SimpleSsdpClient mSsdpClient;

    private final Handler mHandler;
    private Thread mConnectThread = null;
    private int mState;

    SampleApplication mApp;

    private final WifiManager mWifiManager;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_SEARCH = 1;     // now listening for incoming connections
    public static final int STATE_NO_CONF_EXIST = 2;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 3; // now initiating an outgoing connection
    public static final int STATE_CONNECTING_ERROR = 4; // now initiating an outgoing connection
    public static final int STATE_SEARCH_DEVICE = 5; // now initiating an outgoing connection
    public static final int STATE_SEARCH_ERROR = 7;  // now connected to a remote device
    public static final int STATE_CONNECTED = 6;  // now connected to a remote device


    public WifiService(WifiManager wifiManager, SampleApplication app, Handler handler) {

        mApp = app;

        mWifiManager = wifiManager;
        mState = STATE_NONE;
        mHandler = handler;

        IntentFilter filter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mSsdpClient = new SimpleSsdpClient();

    }

    public BroadcastReceiver getReceiver() {
        return mReceiver;
    }


    private synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;

        // Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    private synchronized  void setDevice(ServerDevice device) {
        String name = device.getFriendlyName();
        Log.d(TAG, "setDevice() " + name);
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, name);
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        SimpleRemoteApi remoteApi = mApp.getRemoteApi();
        if (remoteApi == null) {
            mApp.setRemoteApi(new SimpleRemoteApi(device));
        }
    }


    public synchronized void connect(final String ssid) {

        /*
        final String bssidPattern = data.getExtras()
                .getString(WifiListActivity.DEVICE_BSSID);
        */

        mConnectThread = new Thread() {

            @Override
            public void run() {
                setState(STATE_SEARCH);

                List<WifiConfiguration> confList = mWifiManager.getConfiguredNetworks();
                WifiConfiguration targetConf = null;
                for (WifiConfiguration conf : confList) {
                    String tempSsid = conf.SSID.trim().replace("\"", "");
                    if (!tempSsid.matches(ssid)) {
                        continue;
                    }
                    targetConf = conf;
                    break;
                }

                if (targetConf == null) {
                    setState(STATE_NO_CONF_EXIST);
                    return;
                }

                // mProgressBar.setVisibility(View.GONE);
                WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

                if (wifiInfo.getBSSID() != targetConf.BSSID) {
                    boolean result = mWifiManager.enableNetwork(targetConf.networkId, true);
                    if (!result) {
                        setState(STATE_CONNECTING_ERROR);
                        return;
                    }
                    else {
                        setState(STATE_CONNECTING);
                    }
                }
                else {
                    searchDevices();
                }
            };
        };
        mConnectThread.start();

    }

    public void searchDevices() {
        setState(STATE_SEARCH_DEVICE);

        ServerDevice device = null;
        try {
            device = mSsdpClient.search();
        }
        catch (IOException e) {
            setState(STATE_SEARCH_ERROR);
            return;
        }

        setDevice(device);
        setState(STATE_CONNECTED);
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
                        new Thread() {
                            @Override
                            public void run() {
                                searchDevices();
                            }
                        }.start();
                    }
                }
                else {
                    Log.d(TAG, "null state");
                }
            }
        }
    };
    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }
}

/*

            @Override
            public void onDeviceFound(final ServerDevice device) {
                // Called by non-UI thread.

                Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
                Bundle bundle = new Bundle();
                bundle.putString(Constants.DEVICE_NAME, device.getName());
                msg.setData(bundle);
                mHandler.sendMessage(msg);

                connectToCamera(device);

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


 */