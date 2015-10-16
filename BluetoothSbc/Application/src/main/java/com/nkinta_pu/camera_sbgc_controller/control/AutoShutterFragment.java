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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.support.v7.widget.GridLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

// import android.os.SytemClock;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;
// import com.google.vrtoolkit.cardboard.HeadTransform;

class AngleInfo {

    String mLabel;
    float[] mRot = new float[3];

    AngleInfo(String label, float[] rot) {
        mLabel = label;
        mRot = rot;
    }

    float[] getRadian() {
        float[] rot = new float[mRot.length];
        for (int i = 0; i < mRot.length; ++i) {
            rot[i] = mRot[i] / 180.0f * (float)Math.PI;
        }
        return rot;
    }

    String getLabel() {
        return mLabel;
    }

}



/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class AutoShutterFragment extends ControllerFragment {

    private SimpleBgcControl mSimpleBgcControl = null;

    private FloatValue mSpeedValue = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_auto_shutter, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SampleApplication app = (SampleApplication) getActivity().getApplication();
        mSimpleBgcControl = app.getSimpleBgcControl();

        mSpeedValue = createSeekController(
                (SeekBar) view.findViewById(R.id.speed_seek_bar),
                (TextView) view.findViewById(R.id.speed_text_view),
                40, 0.025f);

        // mConversationView = (ListView) view.findViewById(R.id.in);
        // mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        // mSendButton = (Button) view.findViewById(R.id.button_send);
        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(4);

        final MainActivity activity = (MainActivity)getActivity();
        final ArrayList<AngleInfo> angleList = new ArrayList<>();

        for (int i = 0; i < 12; ++i) {
            angleList.add(new AngleInfo("p" + String.format("%3.0f", 00.0f) + "y" + String.format("%3.0f", i * 30.0f), new float[]{0, 0, i * 30f}));
        }
        for (int i = 0; i < 12; ++i) {
            angleList.add(new AngleInfo("p" + String.format("%3.0f", 30.0f) + "y" + String.format("%3.0f", 360 - i * 30.0f), new float[]{0, 30f, 360 - i * 30f}));
        }
        for (int i = 0; i < 6; ++i) {
            angleList.add(new AngleInfo("p" + String.format("%3.0f", 60.0f) + "y" + String.format("%3.0f", i * 60.0f), new float[]{0, 60f, i * 60f}));
        }

        // angleList.add(new AngleInfo("yaw90", new float[]{0, 0, 90}));


        if (gridLayout == null) {
            return;
        }

        Button allButton = new Button(activity);

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams();// gridLayout.getLayoutParams()
        params.columnSpec = GridLayout.spec(0, 4);
        allButton.setLayoutParams(params);

        allButton.setText("all");
        gridLayout.addView(allButton);
        allButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                final CommandDispatcher cd = mSimpleBgcControl.getCommandDispatcher();
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        float speed = mSpeedValue.value;
                        for (final AngleInfo v : angleList) {
                            if (cd.existNextCommand()) return;
                            mSimpleBgcControl.moveSync(new float[]{speed, speed, speed}, v.getRadian());
                            if (cd.existNextCommand()) return;
                            mSimpleBgcControl.waitUntilStop();
                            if (cd.existNextCommand()) return;
                            activity.takeAndFetchPicture();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
                cd.setCommand(runnable);
            }
        });


        for (final AngleInfo v : angleList) {

            Button button = new Button(activity);
            button.setText(v.getLabel());
            gridLayout.addView(button);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View tempView) {

                    final CommandDispatcher cd = mSimpleBgcControl.getCommandDispatcher();
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            float speed = mSpeedValue.value;
                            if (cd.existNextCommand()) return;
                            mSimpleBgcControl.moveSync(new float[]{speed, speed, speed}, v.getRadian());
                            if (cd.existNextCommand()) return;
                            mSimpleBgcControl.waitUntilStop();
                            activity.takeAndFetchPicture();
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    cd.setCommand(runnable);
                }
            });



        }


    }

}
