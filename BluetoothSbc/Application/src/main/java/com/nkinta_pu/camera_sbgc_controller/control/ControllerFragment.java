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

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

import com.nkinta_pu.camera_sbgc_controller.R;

// import android.os.SytemClock;
// import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;
// import com.google.vrtoolkit.cardboard.HeadTransform;


/**
 * This fragment controls Bluetooth to communicate with other devices.
 */
public class ControllerFragment extends Fragment {

    static protected void createSeekController(
            final SeekBar seekBar,
            final TextView textView,
            final float min,
            final float defaultValue,
            final float max,
            final MainParameter.FloatValue storedValue
    ) {

        // LinearLayout linearLayout = (LinearLayout) view.findViewById(parentLayoutId);
        // View seekControlLayout = getLayoutInflater(savedInstanceState).inflate(R.layout.speed_seek_control, linearLayout);

        storedValue.value = defaultValue;

        final float multiple = (max - min) / 100;

        // final LinearLayout linearLayout = (LinearLayout) view.findViewById(id);
        // final SeekBar seekBar = (SeekBar) linearLayout.findViewById(R.id.speed_seek_bar);
        // final TextView textView = (TextView) linearLayout.findViewById(R.id.speed_text_view);
        textView.setText(String.format("%3.2f", storedValue.value));

        seekBar.setProgress((int) (defaultValue / multiple));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                textView.setText(String.format("%3.2f", min + progress * multiple));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                storedValue.value = min + seekBar.getProgress() * multiple;
            }
        });

    }

    static protected void createStoreCallbackSpinner(final Spinner spinner, final MainParameter.IntValue storedValue) {

        spinner.setOnItemSelectedListener(null);
        spinner.setSelection(storedValue.value);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView parent, View view, int position,long id) {
                Spinner spinner = (Spinner) parent;
                // String str = spinner.getSelectedItem().toString();
                storedValue.value = (int)spinner.getSelectedItemId();
            }
            @Override
            public void onNothingSelected(AdapterView parent) {
            }
        });
    }

    static protected void createStoreCallbackSwitch(final Switch switchView, final MainParameter.BooleanValue storedValue) {

        switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                storedValue.value = isChecked;
            }
        });
    }
}
