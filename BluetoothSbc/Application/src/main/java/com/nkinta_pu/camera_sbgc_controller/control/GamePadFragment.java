package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.util.FloatMath;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kanenao on 2015/09/24.
 */
public class GamePadFragment extends ControllerFragment {

    private BluetoothService mChatService = null;

    private ArrayBlockingQueue<byte[]> mBluetoothBlockingQueue = null;

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

        MainParameter mainParameter = app.getMainParameter();
        final MainParameter.ControlGamePadParam param = mainParameter.mControlGamePadParam;

        createSeekController(
                (SeekBar) view.findViewById(R.id.speed_seek_bar),
                (TextView) view.findViewById(R.id.speed_text_view),
                0.0f, param.mSpeed.value, 2.5f, param.mSpeed);

        createSeekController(
                (SeekBar) view.findViewById(R.id.offset_seek_bar),
                (TextView) view.findViewById(R.id.offset_text_view),
                0.0f, param.mDeadBand.value, 1.0f, param.mDeadBand);

        createSeekController(
                (SeekBar) view.findViewById(R.id.expo_seek_bar),
                (TextView) view.findViewById(R.id.expo_text_view),
                0.0f, param.mExpo.value, 20.0f, param.mExpo);

        String[] padTypeStringList = new String[] {"LX", "LY", "RX", "RY"};

        final Spinner rollPadSpinner = (Spinner)view.findViewById(R.id.roll_pad_spinner);
        createStoreCallbackSpinner(rollPadSpinner, param.mRollId);
        final Spinner pitchPadSpinner = (Spinner)view.findViewById(R.id.pitch_pad_spinner);
        createStoreCallbackSpinner(pitchPadSpinner, param.mPitchId);
        final Spinner yawPadSpinner = (Spinner)view.findViewById(R.id.yaw_pad_spinner);
        createStoreCallbackSpinner(yawPadSpinner, param.mYawId);

        final Switch rollInverseSwitch = (Switch)view.findViewById(R.id.roll_inverse_switch);
        createStoreCallbackSwitch(rollInverseSwitch, param.mRollInverseFlag);
        final Switch pitchInverseSwitch = (Switch)view.findViewById(R.id.pitch_inverse_switch);
        createStoreCallbackSwitch(pitchInverseSwitch, param.mPitchInverseFlag);
        final Switch yawInverseSwitch = (Switch)view.findViewById(R.id.yaw_inverse_switch);
        createStoreCallbackSwitch(yawInverseSwitch, param.mYawInverseFlag);

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

        // SampleApplication app = (SampleApplication)getActivity().getApplication();


        activity.setJoyPadJob(
            new GamePadJob() {
                @Override
                public void doCommand(final float[] v) {

                    inputValueTextView.setText(INPUT_VALUE_STRING + "lx ly rx ry-> "
                                    + String.format("%3.2f", v[0]) + " - " + String.format("%3.2f", v[1])
                                    + String.format("%3.2f", v[2]) + " - " + String.format("%3.2f", v[3])
                    );
                    int rollSpinnerId = (int)rollPadSpinner.getSelectedItemId();
                    int pitchSpinnerId = (int)pitchPadSpinner.getSelectedItemId();
                    int yawSpinnerId = (int)yawPadSpinner.getSelectedItemId();
                    yawInverseSwitch.isChecked()

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
