/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller.camera;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nkinta_pu.camera_sbgc_controller.R;

/**
 * An Activity class of Sample Camera screen.
 */
public class CameraFragment extends Fragment {

    CameraRecordFragment mCameraRecordFragment;

    CameraDeviceFragment mCameraDeviceFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_camera, null);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();

            mCameraDeviceFragment = new CameraDeviceFragment();
            transaction.add(R.id.camera_root, mCameraDeviceFragment);

            mCameraRecordFragment = new CameraRecordFragment();
            transaction.add(R.id.camera_root, mCameraRecordFragment);
            transaction.commit();

        }
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
