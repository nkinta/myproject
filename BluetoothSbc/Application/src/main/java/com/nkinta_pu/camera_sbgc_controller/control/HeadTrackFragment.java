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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
// import android.os.SytemClock;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;

// import com.google.vrtoolkit.cardboard.HeadTransform;

import java.lang.Math;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class HeadTrackFragment extends ControllerFragment {

    private HeadTrackHelper mHeadTrackHelper = null;

    private SimpleBgcControl mSimpleBgcControl = null;

    private LooperManager mUiUpdateLooper = null;

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
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mUiUpdateLooper.stop();
        mHeadTrackHelper.onStop();
    }

    @Override
    public void onDestroy() {
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
        mSimpleBgcControl = app.getSimpleBgcControl();
        final MainParameter.HeadTrackParam param = app.getMainParameter().mHeadTrackParam;


        final MainActivity activity = (MainActivity)getActivity();

        final Switch switchButton = (Switch) view.findViewById(R.id.start);
        final TextView headTrackParam = (TextView) view.findViewById(R.id.rpy_param_text_view);

        // final TextView headTrackParam = (TextView) view.findViewById(R.id.headTrackParam);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final float[] angle = mHeadTrackHelper.getLastAngle();
                if (angle == null) {
                    return;
                }

                final float[] degree = new float[3];
                for (int i = 0; i < angle.length; ++i) {
                    degree[i] = angle[i] * 180 / (float) Math.PI;
                }

                if (param.mSpeed == null) {
                    return;
                }
                final float[] speed = new float[]{param.mSpeed.value, param.mSpeed.value, param.mSpeed.value};

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSimpleBgcControl.move(speed, angle);
                        headTrackParam.setText("r = " + String.format("%8.3f", degree[0]) + ", p = " + String.format("%8.3f", degree[1]) + ", y = " + String.format("%8.3f", degree[2]));
                    }
                });

            }
        };

        mUiUpdateLooper = new LooperManager(runnable, "updateui", 200);

        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true) {
                    mHeadTrackHelper.onStart();
                    mUiUpdateLooper.start();
                } else {
                    mUiUpdateLooper.stop();
                    mHeadTrackHelper.onStop();
                }
            }


        });

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
                        mHeadTrackHelper.setRoll(0f);
                    } else if (spinnerId == 1) {
                        mHeadTrackHelper.setRoll(90f);
                    }
                }
            }

            // not selected Spinner dropdown item
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        createSeekController(
                (SeekBar) view.findViewById(R.id.speed_seek_bar),
                (TextView) view.findViewById(R.id.speed_text_view),
                0f, param.mSpeed.value, 2.5f, param.mSpeed);

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
