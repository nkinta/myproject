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
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
// import android.support.v7.widget.GridLayout;
// import android.os.SytemClock;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;

// import com.google.vrtoolkit.cardboard.HeadTransform;

import java.util.ArrayList;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class MessageFragment extends Fragment {

    class CommandInfo {

        private final String mLabel;
        private final Runnable mRunnable;

        CommandInfo(String label,  Runnable runnable) {
            mLabel = label;
            mRunnable = runnable;
        }

        public String getLabel() {
            return mLabel;
        }

        public Runnable getRunnable() {
            return mRunnable;
        }

    }

    private SimpleBgcControl mSimpleBgcControl = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_message, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SampleApplication app = (SampleApplication) getActivity().getApplication();
        mSimpleBgcControl = app.getSimpleBgcControl();

        // GridLayout frameLayout = (GridLayout) view.findViewById(R.id.control);

        final MainActivity activity = (MainActivity)getActivity();
        ArrayList<CommandInfo> commandList = new ArrayList<CommandInfo>();

        Button tempButton;
        tempButton = (Button) view.findViewById(R.id.button_motor_toggle);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setMotorPower(false);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_calib_camera_acc);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.calibrationAcc(0);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_calib_camera_gyro);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.calibrationGyro(0);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_calib_frame_acc);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.calibrationAcc(1);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_calib_frame_gyro);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.calibrationGyro(1);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_follow_yaw_on);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setFollowYaw(true);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_follow_pitch_roll_on);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setFollowPitchRoll(true);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_follow_yaw_off);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setFollowYaw(false);
            }
        });

        tempButton = (Button) view.findViewById(R.id.button_follow_pitch_roll_off);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setFollowPitchRoll(false);
            }
        });

        tempButton = (Button) view.findViewById(R.id.set_profile_button_1);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setCurrentProfile(0);
            }
        });

        tempButton = (Button) view.findViewById(R.id.set_profile_button_2);
        tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View tempView) {
                mSimpleBgcControl.setCurrentProfile(1);
            }
        });

        // mProfileButton = (Button) view.findViewById(R.id.button);
    }

}
