/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;

/**
 * An Activity class of Sample Camera screen.
 */
public class CameraFragment extends Fragment {

    CameraRecordFragment mCameraRecordFragment;

    CameraDeviceFragment mCameraDeviceFragment;

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 3;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_camera, null);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            mCameraDeviceFragment = new CameraDeviceFragment();
            transaction.add(R.id.camera_root, mCameraDeviceFragment);

            mCameraRecordFragment = new CameraRecordFragment();
            transaction.add(R.id.camera_root, mCameraRecordFragment);
            transaction.commit();

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final MainActivity activity = (MainActivity)getActivity();
        switch (item.getItemId()) {
            case R.id.wifi_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Toast.makeText(activity, //
                        "connect wifi", //
                        Toast.LENGTH_SHORT).show(); //

                Intent serverIntent = new Intent(getActivity(), WifiListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
        }
        return false;
    }

    public CameraRecordFragment getCameraRecordFragment() {
        return mCameraRecordFragment;
    }

    public CameraDeviceFragment getCameraDeviceFragment() {
        return mCameraDeviceFragment;
    }

    public void startCamera() {
        // CameraRecordFragment cameraRecordFragment = (CameraRecordFragment) getFragmentManager().findFragmentById(R.id.camera_record);
        if (mCameraRecordFragment != null) {
            mCameraRecordFragment.onPause();
            mCameraRecordFragment.onResume();
        }
    }

    public void stopCamera() {
        // CameraRecordFragment cameraRecordFragment = (CameraRecordFragment) getFragmentManager().findFragmentById(R.id.camera_record);
        if (mCameraRecordFragment != null) {
            mCameraRecordFragment.onPause();
        }
    }

}
