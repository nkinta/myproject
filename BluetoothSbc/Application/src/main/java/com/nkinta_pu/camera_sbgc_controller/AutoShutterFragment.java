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

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;

// import android.os.SytemClock;
// import com.nkinta_pu.camera_sbgc_controller.HeadTrackHelper;
// import com.google.vrtoolkit.cardboard.HeadTransform;

class AngleInfo {

    String mLabel;
    float[] mRot = new float[3];

    AngleInfo(String label, float[] rot) {
        mLabel = label;
        mRot = rot;
    }

    String getLabel() {
        return mLabel;
    }

}



/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class AutoShutterFragment extends Fragment {

    private HeadTrackHelper mHeadTrackHelper = null;

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
        View view = inflater.inflate(R.layout.fragment_bluetooth_chat, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // mConversationView = (ListView) view.findViewById(R.id.in);
        // mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        // mSendButton = (Button) view.findViewById(R.id.button_send);
        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(4);

        final MainActivity activity = (MainActivity)getActivity();
        ArrayList<AngleInfo> angleList = new ArrayList<>();

        angleList.add(new AngleInfo("yaw00", new float[]{0, 00, 0}));
        angleList.add(new AngleInfo("yaw30", new float[]{0, 30, 0}));
        angleList.add(new AngleInfo("yaw60", new float[]{0, 60, 0}));
        angleList.add(new AngleInfo("yaw90", new float[]{0, 90, 0}));

        if (gridLayout == null) {
            return;
        }

        for (AngleInfo v: angleList) {

            Button button = new Button(activity);
            button.setText(v.getLabel());
            gridLayout.addView(button);

            for (float rotValue: v.mRot) {
                EditText editText = new EditText(activity);
                editText.setText(String.format("%4.1f", rotValue));
                gridLayout.addView(editText);
            }

            byte[] data = {(byte)0x00};
            CommandInfo commandInfo = new CommandInfo(v.getLabel(), (byte) 0x43, data);
            final byte[] commandData = commandInfo.getCommandData();
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View tempView) {
                    // Send a message using content of the edit text widget
                    View view = getView();
                    if (null != view) {
                        activity.send_bluetooth_message(commandData);
                    }
                }
            });

        }


    }

}
