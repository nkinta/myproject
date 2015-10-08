/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package com.nkinta_pu.camera_sbgc_controller;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ViewAnimator;
import android.support.v4.view.ViewPager;

import com.example.android.common.activities.SampleActivityBase;
import com.example.android.common.logger.Log;
import com.example.android.common.logger.LogFragment;
import com.example.android.common.logger.LogWrapper;
import com.example.android.common.logger.MessageOnlyLogFilter;
import com.nkinta_pu.camera_sbgc_controller.camera.CameraFragment;
import com.nkinta_pu.camera_sbgc_controller.control.BluetoothConnectFragment;
import com.nkinta_pu.camera_sbgc_controller.control.JoyPadJob;
import com.nkinta_pu.camera_sbgc_controller.control.PagerAdapter;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends SampleActivityBase {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mLogShown;

    ViewPager mViewPager;
    CameraFragment mCameraFragment;
    BluetoothConnectFragment mBluetoothChatFragment;

    JoyPadJob mJoyPadJob = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        // mCameraFragment = (Fragment)(getFragmentManager().findFragmentById(R.id.fragment));

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mBluetoothChatFragment = new BluetoothConnectFragment();
            transaction.add(R.id.sample_main_layout, mBluetoothChatFragment);
            transaction.commit();

            // FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            // mCameraFragment = new CameraFragment();
            // transaction.replace(R.id.fragment, mCameraFragment);
            // transaction.commit();

        }

    }

    public void takeAndFetchPicture() {
        mCameraFragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);
        mCameraFragment.getCameraRecordFragment().takeAndFetchPicture();
    }

    public void startCamera() {
        mCameraFragment = (CameraFragment) getSupportFragmentManager().findFragmentById(R.id.camera_fragment);
        if (mCameraFragment != null) {
            mCameraFragment.startCamera();
        }
    }

    public void stopCamera() {
        if (mCameraFragment != null) {
            mCameraFragment.stopCamera();
        }
    }

    public void setJoyPadJob(JoyPadJob job) {
        mJoyPadJob = job;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem logToggle = menu.findItem(R.id.menu_toggle_log);
        logToggle.setVisible(findViewById(R.id.sample_output) instanceof ViewAnimator);
        logToggle.setTitle(mLogShown ? R.string.sample_hide_log : R.string.sample_show_log);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                mLogShown = !mLogShown;
                ViewAnimator output = (ViewAnimator) findViewById(R.id.sample_output);
                if (mLogShown) {
                    output.setVisibility(View.VISIBLE);
                } else {
                    output.setVisibility(View.GONE);
                }
                supportInvalidateOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Create a chain of targets that will receive log data */
    @Override
    public void initializeLogging() {

        // Wraps Android's native log framework.
        LogWrapper logWrapper = new LogWrapper();
        // Using Log, front-end to the logging chain, emulates android.util.log method signatures.
        Log.setLogNode(logWrapper);

        // Filter strips out everything except the message text.
        MessageOnlyLogFilter msgFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(msgFilter);

        // On screen logging via a fragment with a TextView.
        LogFragment logFragment = (LogFragment) getSupportFragmentManager()
                .findFragmentById(R.id.log_fragment);
        msgFilter.setNext(logFragment.getLogView());

        Log.i(TAG, "Ready");

    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent e){

        if (!((e.getSource() & InputDevice.SOURCE_DPAD)
                != InputDevice.SOURCE_DPAD)) {
            return super.dispatchGenericMotionEvent(e);
        }

        if (mJoyPadJob == null) {
            return false;
        }
        // Use the hat axis value to find the D-pad direction
        MotionEvent motionEvent = (MotionEvent) e;
        float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
        float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

        mJoyPadJob.doCommand(new float[] {xaxis, yaxis});
            // Toast.makeText(this, "xy -> " + String.format("%3.2f",xaxis) + " - " +  String.format("%3.2f",yaxis), Toast.LENGTH_SHORT).show();
        return true;
    }

    public static boolean isDpadDevice(InputEvent event) {
        // Check that input comes from a device with directional pads.
        if ((event.getSource() & InputDevice.SOURCE_DPAD)
                != InputDevice.SOURCE_DPAD) {
            return true;
        } else {
            return false;
        }
    }
}
