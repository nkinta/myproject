package com.nkinta_pu.camera_sbgc_controller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayout;
import android.view.InputDevice;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by kanenao on 2015/09/24.
 */
public class GamePadFragment extends Fragment {

    private BluetoothChatService mChatService = null;

    private ArrayBlockingQueue<byte[]> mBluetoothBlockingQueue = null;

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
        View view = inflater.inflate(R.layout.fragment_control, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MainActivity activity = (MainActivity) getActivity();
        SampleApplication app = (SampleApplication) activity.getApplication();

        final TextView speedTextView = new TextView(activity);
        speedTextView.setText("-");

        final Switch switchButton = new Switch(activity);
        switchButton.setText("GAME_PAD");

        final SeekBar speedSeekBar = new SeekBar(activity);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        speedSeekBar.setLayoutParams(params);
        speedSeekBar.setProgress(0); speedSeekBar.setMax(100);

        final TextView textView = new TextView(activity);
        textView.setText("-");

        GridLayout gridLayout = (GridLayout) view.findViewById(R.id.control);
        gridLayout.setColumnCount(2);

        gridLayout.addView(switchButton);
        gridLayout.addView(speedTextView);
        gridLayout.addView(speedSeekBar);
        gridLayout.addView(textView);




        activity.setJoyPadJob(
                new JoyPadJob() {
                      @Override
                      public void doCommand(float[] v) {
                          textView.setText("x y -> " + String.format("%3.2f", v[0]) + " - " +  String.format("%3.2f", v[1]));
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
