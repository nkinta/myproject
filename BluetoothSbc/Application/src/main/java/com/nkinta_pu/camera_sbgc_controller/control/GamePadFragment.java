package com.nkinta_pu.camera_sbgc_controller.control;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

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

        final TextView textView = new TextView(activity);
        textView.setText("-");



        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(2);

        gridLayout.addView(switchButton);
        gridLayout.addView(speedTextView);
        gridLayout.addView(textView);

        mSpeedValue = createSeekController(view, R.id.speed_seek_control, 40, 0.025f);
        mOffsetValue = createSeekController(view, R.id.offset_seek_control, 20, 0.025f);

        activity.setJoyPadJob(
                new JoyPadJob() {
                    @Override
                    public void doCommand(final float[] v) {
                        textView.setText("x y -> " + String.format("%3.2f", v[0]) + " - " + String.format("%3.2f", v[1]));
                        new Thread() {
                            @Override
                            public void run() {
                                mSimpleBgcControl.setSpeed(new float[]{0f, v[1] * mSpeedValue.value, v[1] * mSpeedValue.value});
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
