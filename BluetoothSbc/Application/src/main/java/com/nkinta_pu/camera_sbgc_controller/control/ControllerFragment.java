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

import com.nkinta_pu.camera_sbgc_controller.R;

// import android.os.SytemClock;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;
// import com.google.vrtoolkit.cardboard.HeadTransform;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class ControllerFragment extends Fragment {

    class FloatValue {
        public float value = 0;
    }


    protected FloatValue createSeekController(View view, int id, final int defaultValue, final float multiple) {

        // LinearLayout linearLayout = (LinearLayout) view.findViewById(parentLayoutId);
        // View seekControlLayout = getLayoutInflater(savedInstanceState).inflate(R.layout.seek_control, linearLayout);

        final FloatValue seekValue = new FloatValue();
        seekValue.value = (float)(defaultValue) * multiple;

        final LinearLayout linearLayout = (LinearLayout) view.findViewById(id);
        final SeekBar seekBar = (SeekBar) linearLayout.findViewById(R.id.speed_seek_bar);
        final TextView textView = (TextView) linearLayout.findViewById(R.id.speed_text_view);
        textView.setText(String.format("%3.2f", seekValue.value));

        seekBar.setProgress(defaultValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                textView.setText(String.format("%3.2f", progress * multiple));
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                seekValue.value = seekBar.getProgress() * multiple;
            }
        });

        return seekValue;

    }

}
