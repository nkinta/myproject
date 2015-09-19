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

package com.nkinta_pu.camera_sbgc_controller;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
// import android.os.SytemClock;

import com.example.android.common.logger.Log;
import com.nkinta_pu.camera_sbgc_controller.utils.DisplayHelper;
// import com.nkinta_pu.camera_sbgc_controller.HeadTrackHelper;

// import com.google.vrtoolkit.cardboard.HeadTransform;

import java.util.ArrayList;
import java.lang.Math;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class HeadTrackFragment extends Fragment {

    private HeadTrackHelper mHeadTrackHelper = null;
    private float mRoll = 0;
    private float[] mBeforeAngle = {};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentActivity activity = getActivity();
        mHeadTrackHelper = new HeadTrackHelper(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        // mHeadTrackHelper.onStart();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        // mHeadTrackHelper.onStop();
    }

    @Override
    public void onDestroy() {
        mHeadTrackHelper.onStop();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_chat, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // mConversationView = (ListView) view.findViewById(R.id.in);
        // mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        // mSendButton = (Button) view.findViewById(R.id.button_send);
        final MainActivity activity = (MainActivity)getActivity();
        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(2);
        // Switch switchButton = (Switch) view.findViewById(R.id.headTrackSwitch);
        // switchButton.set

        final Switch switchButton = new Switch(activity);
        switchButton.setText("HEAD_TRACK");
        gridLayout.addView(switchButton);

        final TextView headTrackParam = new TextView(activity);
        headTrackParam.setText("-");
        gridLayout.addView(headTrackParam);

        // final TextView headTrackParam = (TextView) view.findViewById(R.id.headTrackParam);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){
                if (isChecked == true) {
                    mBeforeAngle = new float[]{};
                    mHeadTrackHelper.onStart();
                }
                else {
                    mHeadTrackHelper.onStop();
                }
            }


        });

        HeadTrackJob job1 = new HeadTrackJob() {
            @Override
            public void doCommand(HeadTransform t) {
                float[] angle = new float[3];
                t.getEulerAngles(mBeforeAngle, mRoll, angle, 0);
                mBeforeAngle = angle;

                float[] degree = new float[3];
                for (int i = 0; i < angle.length; ++i) {
                    degree[i] = angle[i] * 180 / (float)Math.PI;
                }

                headTrackParam.setText("x = " + String.format("%8.3f", degree[0]) + ", y = " + String.format("%8.3f", degree[1]) + ", z = " + String.format("%8.3f", degree[2]));

                CommandInfo command = SimpleBgcUtility.getControlCommand(angle);

                activity.send_bluetooth_message(command.getCommandData());
            }
        };

        mHeadTrackHelper.setJob(job1);

        final Spinner directorySpinner = new Spinner(activity);
        gridLayout.addView(directorySpinner);
        // directorySpinner.setText("HEAD_TRACK");

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter(activity, //
                android.R.layout.simple_spinner_item, new String[] {"default", "->"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setPrompt(getString(R.string.prompt_shoot_mode));
        // selectionShootModeSpinner(directorySpinner, currentMode);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            // selected Spinner dropdown item
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Spinner spinner = (Spinner) parent;
                if (!spinner.isFocusable()) {
                    // ignored the first call, because shoot mode has not
                    // changed
                    spinner.setFocusable(true);
                } else {
                    long spinnerId = spinner.getSelectedItemId();
                    if (spinnerId == 0) {
                        mBeforeAngle = new float[]{};
                        mRoll = 0;
                    }
                    else if (spinnerId == 1) {
                        mBeforeAngle = new float[]{};
                        mRoll = 90;
                    }
                }
            }

            // not selected Spinner dropdown item
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

}
