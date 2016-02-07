package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.util.FloatMath;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kanenao on 2015/09/24.
 */
public class GamePadFragment extends ControllerFragment {

    private BluetoothService mChatService = null;

    private ArrayBlockingQueue<byte[]> mBluetoothBlockingQueue = null;

    FloatValue mSpeedValue = null;
    FloatValue mOffsetValue = null;
    FloatValue mExpoValue = null;

    private static final String OUTPUT_VALUE_STRING = "OUTPUT_VALUE";
    private static final String INPUT_VALUE_STRING = "INPUT_VALUE";

    private SimpleBgcControl mSimpleBgcControl = null;

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
        View view = inflater.inflate(R.layout.fragment_game_pad, null);
        return view;
    }

    static float filter(float v, float speed, float offset, float expo) {
        float pv = speed * FloatMath.pow(Math.max(0.0f, Math.abs(v) - offset), expo);
        float rv;
        if (v > 0) {
            rv = pv;
        }
        else {
            rv = -pv;
        }

        return rv;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        SampleApplication app = (SampleApplication) activity.getApplication();

        mSimpleBgcControl = app.getSimpleBgcControl();

        final TextView speedTextView = new TextView(activity);
        speedTextView.setText("-");

        final Switch switchButton = new Switch(activity);
        switchButton.setText("GAME_PAD");

        final TextView inputValueTextView = new TextView(activity);
        inputValueTextView.setText(OUTPUT_VALUE_STRING);

        final TextView outputValueTextView = new TextView(activity);
        outputValueTextView.setText(INPUT_VALUE_STRING);


        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(2);

        gridLayout.addView(switchButton);
        gridLayout.addView(speedTextView);
        gridLayout.addView(inputValueTextView);

        GridLayout.LayoutParams params =
                new GridLayout.LayoutParams(inputValueTextView.getLayoutParams());

        // params.rowSpec = GridLayout.spec(0, 2);
        params.columnSpec = GridLayout.spec(0, 2);
        inputValueTextView.setLayoutParams(params);

        gridLayout.addView(outputValueTextView);

        mSpeedValue = createSeekController(
                (SeekBar) view.findViewById(R.id.speed_seek_bar),
                (TextView) view.findViewById(R.id.speed_text_view),
                40, 0.025f);
        mOffsetValue = createSeekController(
                (SeekBar) view.findViewById(R.id.offset_seek_bar),
                (TextView) view.findViewById(R.id.offset_text_view),
                10, 0.010f);

        mExpoValue = createSeekController(
                (SeekBar) view.findViewById(R.id.expo_seek_bar),
                (TextView) view.findViewById(R.id.expo_text_view),
                10, 0.20f);

        String[] padTypeStringList = new String[] {"LX", "LY", "RX", "RY"};

        final Spinner rollPadSpinner = (Spinner)view.findViewById(R.id.roll_pad_spinner);
        final Spinner pitchPadSpinner = (Spinner)view.findViewById(R.id.pitch_pad_spinner);
        final Spinner yawPadSpinner = (Spinner)view.findViewById(R.id.yaw_pad_spinner);

        ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter(activity, //
                android.R.layout.simple_spinner_item, padTypeStringList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        rollPadSpinner.setAdapter(adapter);
        rollPadSpinner.setSelection(2);

        pitchPadSpinner.setAdapter(adapter);
        pitchPadSpinner.setSelection(1);

        yawPadSpinner.setAdapter(adapter);
        yawPadSpinner.setSelection(0);

        activity.setJoyPadJob(
            new JoyPadJob() {
                @Override
                public void doCommand(final float[] v) {
                    inputValueTextView.setText(INPUT_VALUE_STRING + "lx ly rx ry-> "
                            + String.format("%3.2f", v[0]) + " - " + String.format("%3.2f", v[1])
                            + String.format("%3.2f", v[2]) + " - " + String.format("%3.2f", v[3])
                    );
                    int rollSpinnerId = (int)rollPadSpinner.getSelectedItemId();
                    int pitchSpinnerId = (int)pitchPadSpinner.getSelectedItemId();
                    int yawSpinnerId = (int)yawPadSpinner.getSelectedItemId();

                    final float roll = filter(v[rollSpinnerId], mSpeedValue.value, mOffsetValue.value, mExpoValue.value);
                    final float pitch = filter(v[pitchSpinnerId], mSpeedValue.value, mOffsetValue.value, mExpoValue.value);
                    final float yaw = filter(v[yawSpinnerId], mSpeedValue.value, mOffsetValue.value, mExpoValue.value);
                    outputValueTextView.setText(OUTPUT_VALUE_STRING + "roll pitch yaw -> "
                            + String.format("%3.2f", roll) + " - " + String.format("%3.2f", pitch) + String.format("%3.2f", yaw)
                    );

                    new Thread() {
                        @Override
                        public void run() {
                            mSimpleBgcControl.setSpeed(new float[]{roll, pitch, yaw});
                        }
                    }.run();
                }
            }
        );

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // MainActivity activity = (MainActivity)getActivity();
    }

    @Override
    public  void onDestroyView () {
        MainActivity activity = (MainActivity) getActivity();
        activity.setJoyPadJob(null);
        super.onDestroyView();
    }

}
