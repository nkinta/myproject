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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.camera.CameraFragment;
import com.nkinta_pu.camera_sbgc_controller.control.ControlViewPager;
import com.nkinta_pu.camera_sbgc_controller.control.GamePadControl;
import com.nkinta_pu.camera_sbgc_controller.control.GamePadJob;
import com.nkinta_pu.camera_sbgc_controller.control.PagerAdapter;
import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MainActivity extends FragmentActivity {

    public static final String TAG = "MainActivity";

    // Whether the Log Fragment is currently shown
    private boolean mCameraControllerShownFlag = true;

    ViewPager mViewPager;
    CameraFragment mCameraFragment;
    // BluetoothConnectFragment mBluetoothConnectFragment;
    // GamePadJob mGamePadJob = null;
    private GamePadControl mGamePadControl = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setAdapter(new PagerAdapter(getSupportFragmentManager()));

        SampleApplication app = (SampleApplication)getApplication();
        mGamePadControl = app.getGamePadControl();

        // mCameraFragment = (Fragment)(getFragmentManager().findFragmentById(R.id.fragment));

        if (savedInstanceState == null) {
            /*
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mBluetoothConnectFragment = new BluetoothConnectFragment();
            transaction.add(R.id.sample_main_layout, mBluetoothConnectFragment);
            transaction.commit();
            */

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

    /*
    public void setJoyPadJob(GamePadJob job) {
        mGamePadJob = job;
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem cameraControllerToggle = menu.findItem(R.id.show_camera_controller);

        cameraControllerToggle.setTitle(mCameraControllerShownFlag ? R.string.hide_camera_controller : R.string.show_camera_controller);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.optionsMenu_01:
                Intent intent1 = new android.content.Intent(this, ConnectPreferenceActivity.class);
                startActivity(intent1);
                return true;
            case R.id.show_camera_controller:
                mCameraControllerShownFlag = !mCameraControllerShownFlag;
                //
                Fragment cameraControllerToggle = getSupportFragmentManager().findFragmentById(R.id.camera_fragment);
                if (mCameraControllerShownFlag) {
                    cameraControllerToggle.getView().setVisibility(View.VISIBLE);
                }
                else {
                    cameraControllerToggle.getView().setVisibility(View.GONE);
                }

                supportInvalidateOptionsMenu();
                return true;
            case R.id.store_parameter:
                storeParameter();
                return true;
            case R.id.restore_parameter:
                restoreParameter();
            return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void storeParameter() {
        SampleApplication app = (SampleApplication)getApplication();

        try {
            MainParameter param =  app.getMainParameter();
            FileOutputStream fos = openFileOutput("SaveData.dat", MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(param);
            oos.close();
        } catch (Exception e) {
            Log.d(TAG, "Store Error");
        }
        Toast.makeText(this, "Parameter Stored", Toast.LENGTH_SHORT).show();
    }

    private void restoreParameter() {
        SampleApplication app = (SampleApplication)getApplication();
        try {
            FileInputStream fis = openFileInput("SaveData.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            MainParameter data = (MainParameter) ois.readObject();
            ois.close();
            app.setMainParameter(data);
        } catch (Exception e) {
            Log.d(TAG, "Restore Error");
        }
        Toast.makeText(this, "Parameter Restored", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent e){

        if (!((e.getSource() & InputDevice.SOURCE_DPAD)
                != InputDevice.SOURCE_DPAD)) {
            return super.dispatchGenericMotionEvent(e);
        }

        mGamePadControl.dispatchGenericMotionEvent(e);
            // Toast.makeText(this, "xy -> " + String.format("%3.2f",xaxis) + " - " +  String.format("%3.2f",yaxis), Toast.LENGTH_SHORT).show();
        return true;
    }

    public void setStatus(int textViewResource, int title) {
        TextView textView = (TextView) findViewById(textViewResource);
        textView.setText(title);
    }

    public void setStatus(int textViewResource, CharSequence title) {
        TextView textView = (TextView) findViewById(textViewResource);
        textView.setText(title);
    }

    /*
    private void setWifiStatus(CharSequence title) {
        TextView textView = (TextView) findViewById(R.id.wifi_status);
        textView.setText(title);
    }
    private void setWifiStatus(int id) {
        TextView textView = (TextView) findViewById(R.id.wifi_status);
        textView.setText(id);
    }
    */

}
