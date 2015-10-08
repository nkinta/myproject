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
import android.support.v7.widget.GridLayout;
// import android.os.SytemClock;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.camera.SampleApplication;
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


    private BluetoothChatService mChatService = null;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SampleApplication app = (SampleApplication) getActivity().getApplication();
        mChatService = app.getBluetoothChatService();

        GridLayout frameLayout = (GridLayout) view.findViewById(R.id.control);

        final MainActivity activity = (MainActivity)getActivity();
        ArrayList<CommandInfo> commandList = new ArrayList<CommandInfo>();

        commandList.add(new CommandInfo("calibGyro", new Runnable() {
            @Override
            public void run() {
                SimpleBgcUtility.calibrationGyro(0, mChatService);
            }
        }));

        commandList.add(new CommandInfo("calibAcc", new Runnable() {
            @Override
            public void run() {
                SimpleBgcUtility.calibrationAcc(0, mChatService);
            }
        }));

        commandList.add(new CommandInfo("getProfile1", new Runnable() {
            @Override
            public void run() {
                SimpleBgcUtility.getProfile(0, mChatService);
            }
        }));

        if (frameLayout == null) {
            return;
        }

        for (final CommandInfo v: commandList) {
            Button button = new Button(activity);
            button.setText(v.getLabel());
            frameLayout.addView(button);

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View tempView) {
                    // Send a message using content of the edit text widget
                    v.getRunnable().run();
                }
            });

        }
        // mProfileButton = (Button) view.findViewById(R.id.button);
    }

}
