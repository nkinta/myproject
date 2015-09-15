/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
            FragmentTransaction transaction1 = getFragmentManager().beginTransaction();
            mCameraRecordFragment = new CameraRecordFragment();
            transaction1.replace(R.id.camera_record, mCameraRecordFragment);
            transaction1.commit();

            FragmentTransaction transaction2 = getFragmentManager().beginTransaction();
            mCameraDeviceFragment = new CameraDeviceFragment();
            transaction2.replace(R.id.camera_remote, mCameraDeviceFragment);
            transaction2.commit();

        }
    }


    public void startCamera() {
        // CameraRecordFragment cameraRecordFragment = (CameraRecordFragment) getFragmentManager().findFragmentById(R.id.camera_record);
        if (mCameraRecordFragment != null) {
            mCameraRecordFragment.onPause();
            mCameraRecordFragment.onResume();
        }
    }

    public void stopCamera() {
        CameraRecordFragment cameraRecordFragment = (CameraRecordFragment) getFragmentManager().findFragmentById(R.id.camera_record);
        if (mCameraRecordFragment != null) {
            mCameraRecordFragment.onPause();
        }
    }

}
