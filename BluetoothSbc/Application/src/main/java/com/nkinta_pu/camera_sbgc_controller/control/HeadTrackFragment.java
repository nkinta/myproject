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

package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
// import android.os.SytemClock;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.camera.SampleApplication;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;

// import com.google.vrtoolkit.cardboard.HeadTransform;

import java.lang.Math;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class HeadTrackFragment extends ControllerFragment {

    private static final float SPEED_MULTIPLE = 0.025f;

    IntValue mSpeedValue = null;

    private HeadTrackHelper mHeadTrackHelper = null;
    private float mRoll = 0;
    private float[] mBeforeAngle = {};

    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentActivity activity = getActivity();
        SampleApplication application = (SampleApplication)activity.getApplication();
        mHeadTrackHelper = application.getHeadTrackHelper();
        if (mHeadTrackHelper == null) {
            mHeadTrackHelper = new HeadTrackHelper(activity);
            application.setHeadTrackHelper(mHeadTrackHelper);
        }
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
        View view = inflater.inflate(R.layout.fragment_head_track, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.root);

        SampleApplication app = (SampleApplication) getActivity().getApplication();
        mChatService = app.getBluetoothChatService();
        final MainActivity activity = (MainActivity)getActivity();

        // mConversationView = (ListView) view.findViewById(R.id.in);
        // mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        // mSendButton = (Button) view.findViewById(R.id.button_send);

        // Switch switchButton = (Switch) view.findViewById(R.id.headTrackSwitch);
        // switchButton.set
        /*
        final MainActivity activity = (MainActivity)getActivity();
        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(2);

        final Switch switchButton = new Switch(activity);
        switchButton.setText("HEAD_TRACK");
        gridLayout.addView(switchButton);

        final TextView headTrackParam = new TextView(activity);
        headTrackParam.setText("-");
        gridLayout.addView(headTrackParam);
        */

        final Switch switchButton = (Switch) view.findViewById(R.id.start);
        final TextView headTrackParam = (TextView) view.findViewById(R.id.rpy_param_text_view);

        // final TextView headTrackParam = (TextView) view.findViewById(R.id.headTrackParam);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mBeforeAngle = new float[]{};
                    mHeadTrackHelper.onStart();
                } else {
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

                headTrackParam.setText("r = " + String.format("%8.3f", degree[0]) + ", p = " + String.format("%8.3f", degree[1]) + ", y = " + String.format("%8.3f", degree[2]));

                if (mSpeedValue == null) {
                    return;
                }
                float[] speed = new float[] {mSpeedValue.value  * SPEED_MULTIPLE, mSpeedValue.value  * SPEED_MULTIPLE, mSpeedValue.value  * SPEED_MULTIPLE};
                SimpleBgcUtility.move(speed, angle, mChatService);
                // mChatService.send(command.getCommandData());
            }
        };


        mHeadTrackHelper.setJob(job1);

        final Spinner directorySpinner = (Spinner) view.findViewById(R.id.direction_spinner);
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

        mSpeedValue = createSeekController(view, 40, 0.025f);

        /*
        final SeekBar speedSeekBar = (SeekBar) view.findViewById(R.id.speed_seek_bar);
        final TextView speedTextView = (TextView) view.findViewById(R.id.speed_text_view);
        speedTextView.setText(String.format("%3.2f", mSpeed * SPEED_MULTIPLE));

        speedSeekBar.setProgress(mSpeed);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                speedTextView.setText(String.format("%3.2f", progress * SPEED_MULTIPLE));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSpeed = seekBar.getProgress();

            }
        });
        */


    }

}
