/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;

import java.util.List;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class WifiListActivity extends Activity {

    /**
     * Tag for Log
     */
    private static final String TAG = "DeviceListActivity";

    private static final int TRY_INTERVAL = 1000; // msec

    private static final int TRY_COUNT = 10;

    private static final int DISCOVERY_START = 0;

    private static final int DISCOVERY_FINISHED = 1;

    private static final int FOUND = 2;
    /**
     * Return Intent extra
     */
    public static String DEVICE_SSID = "device_ssid";

    public static String DEVICE_BSSID = "device_bssid";

    /**
     * Member fields
     */
    private WifiManager mWifiManager;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setVisibility(View.GONE);
                new Thread() {

                    @Override
                    public void run() {
                        try {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setStatus(DISCOVERY_START);
                                }
                            });
                            for (int i = 0; i < TRY_COUNT; ++i) {
                                final List<ScanResult> accessPointList = doDiscovery();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateListUi(accessPointList);
                                    }
                                });
                                sleep(TRY_INTERVAL);
                            }
                        }
                        catch (InterruptedException e) {
                            // do nothing.
                            Log.d(TAG, "search() InterruptedException :", e);
                        }
                        finally {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setStatus(DISCOVERY_FINISHED);
                                }
                            });
                        }

                    }
                }.start();

            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
        mPairedDevicesArrayAdapter =
                new ArrayAdapter<String>(this, R.layout.device_name);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        this.registerReceiver(mReceiver, filter);


        mWifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        mWifiManager.startScan();
        List<ScanResult> accessPointList = mWifiManager.getScanResults();

        updateListUi(accessPointList);
    }

    private void updateListUi(List<ScanResult> accessPointList) {
        if (accessPointList.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (ScanResult device : accessPointList) {
                String ssidPattern = "DIRECT-.*";
                if (!device.SSID.matches(ssidPattern)) {
                    continue;
                }
                mPairedDevicesArrayAdapter.add(device.SSID + "_" + device.BSSID);
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mWifiManager != null) {
            // mWifiManager.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    private void setStatus(int status) {
        // Indicate scanning in the title
        if (status == DISCOVERY_START) {
            Log.d(TAG, "doDiscovery()");
            setProgressBarIndeterminateVisibility(true);
            setTitle(R.string.scanning);
            findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
        }
        else if (status == DISCOVERY_FINISHED) {
            Log.d(TAG, "doDiscovery()");
            setProgressBarIndeterminateVisibility(true);
            setTitle(R.string.scanning);
            findViewById(R.id.title_new_devices).setVisibility(View.INVISIBLE);
        }
        else {

        }
        // Turn on sub-title for new devices

    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private List<ScanResult> doDiscovery() {
        mWifiManager.startScan();
        List<ScanResult> accessPointList = mWifiManager.getScanResults();
        return accessPointList;
    }

    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String ssid = info.substring(0, info.length() - 17 - 1);
            String bssid = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(DEVICE_SSID, ssid);
            intent.putExtra(DEVICE_BSSID, bssid);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION.equals(action)) {
                Log.d(TAG, "SUPPLICANT_CONNECTION_CHANGE_ACTION");
            } else if (WifiManager.NETWORK_IDS_CHANGED_ACTION.equals(action)) {
                Log.d(TAG, "NETWORK_IDS_CHANGED_ACTION");
            }
        }
    };

}


// List<WifiConfiguration> confList = mWifiManager.getConfiguredNetworks();
// Get the local Bluetooth adapter
// mBtAdapter = BluetoothAdapter.getDefaultAdapter();
// Get a set of currently paired devices
// Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
// If there are paired devices, add each one to the ArrayAdapter