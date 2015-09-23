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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.support.v7.widget.GridLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
// import android.os.SytemClock;

import com.example.android.common.logger.Log;
// import com.nkinta_pu.camera_sbgc_controller.HeadTrackHelper;

// import com.google.vrtoolkit.cardboard.HeadTransform;

import java.util.ArrayList;
import java.lang.Math;

/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class MessageFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_control, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        GridLayout frameLayout = (GridLayout) view.findViewById(R.id.control);

        final MainActivity activity = (MainActivity)getActivity();
        ArrayList<CommandInfo> commandList = new ArrayList<CommandInfo>();

        byte[] data1 = {(byte)0x00};
        commandList.add(new CommandInfo("Profile1", (byte) 0x15, data1));

        byte[] data2 = {(byte)0x01};
        commandList.add(new CommandInfo("Profile2", (byte) 0x15, data2));

        byte[] data3 = {(byte)0x02};
        commandList.add(new CommandInfo("Profile2", (byte) 0x15, data3));

        if (frameLayout == null) {
            return;
        }

        for (final CommandInfo v: commandList) {
            Button button = new Button(activity);
            button.setText(v.getLabel());
            frameLayout.addView(button);

            final byte[] commandData = v.getCommandData();
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View tempView) {
                    // Send a message using content of the edit text widget
                    View view = getView();
                    if (null != view) {
                        // byte[] send = {(byte) 0x3E, (byte) 0x15, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00};
                        activity.send_bluetooth_message(v.getCommandData());
                    }
                }
            });

        }
        // mProfileButton = (Button) view.findViewById(R.id.button);
    }

}
