package com.nkinta_pu.camera_sbgc_controller.control;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.nkinta_pu.camera_sbgc_controller.MainActivity;
import com.nkinta_pu.camera_sbgc_controller.R;
import com.nkinta_pu.camera_sbgc_controller.SampleApplication;
import com.nkinta_pu.camera_sbgc_controller.param.MainParameter;

/**
 * Created by NK on 2016/03/28.
 */
public class VirtualGamePadFragment extends ControllerFragment {

    RelativeLayout layout_joystick;
    ImageView image_joystick, image_border;
    TextView textView1, textView2;

    JoyStickClass js;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_virtual_game_pad, null);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ControlScrollView scrollView = (ControlScrollView)getActivity().findViewById(R.id.scroll_view);
        final ControlViewPager viewPager = (ControlViewPager)getActivity().findViewById(R.id.view_pager);

        MainActivity activity = (MainActivity) getActivity();
        SampleApplication app = (SampleApplication) activity.getApplication();
        MainParameter mainParameter = app.getMainParameter();
        final MainParameter.ControlVirtualGamePadParam param = mainParameter.mControlVirtualGamePadParam;

        createSeekController(
                (SeekBar) view.findViewById(R.id.speed_seek_bar),
                (TextView) view.findViewById(R.id.speed_text_view),
                0.0f, param.mSpeed.value, 2.5f, param.mSpeed);

        createSeekController(
                (SeekBar) view.findViewById(R.id.dead_band_seek_bar),
                (TextView) view.findViewById(R.id.dead_band_text_view),
                0.0f, param.mDeadBand.value, 1.0f, param.mDeadBand);

        createSeekController(
                (SeekBar) view.findViewById(R.id.expo_seek_bar),
                (TextView) view.findViewById(R.id.expo_text_view),
                0.0f, param.mExpo.value, 20.0f, param.mExpo);

        textView1 = (TextView)view.findViewById(R.id.textView1);
        textView2 = (TextView)view.findViewById(R.id.textView2);

        layout_joystick = (RelativeLayout)view.findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getActivity().getApplicationContext(), layout_joystick, R.drawable.icon);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                js.drawStick(arg1);
                if(arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    viewPager.setScrollingEnabled(false);
                    scrollView.setScrollingEnabled(false);
                    textView1.setText("X : " + String.valueOf(js.getX()));
                    textView2.setText("Y : " + String.valueOf(js.getY()));
                }
                else if(arg1.getAction() == MotionEvent.ACTION_UP) {
                    viewPager.setScrollingEnabled(true);
                    scrollView.setScrollingEnabled(true);
                    textView1.setText("X :");
                    textView2.setText("Y :");
                }
                return true;
            }
        });
    }
}

/*

textView3.setText("Angle : " + String.valueOf(js.getAngle()));
textView4.setText("Distance : " + String.valueOf(js.getDistance()));

int direction = js.get8Direction();
if(direction == JoyStickClass.STICK_UP) {
    textView5.setText("Direction : Up");
} else if(direction == JoyStickClass.STICK_UPRIGHT) {
    textView5.setText("Direction : Up Right");
} else if(direction == JoyStickClass.STICK_RIGHT) {
    textView5.setText("Direction : Right");
} else if(direction == JoyStickClass.STICK_DOWNRIGHT) {
    textView5.setText("Direction : Down Right");
} else if(direction == JoyStickClass.STICK_DOWN) {
    textView5.setText("Direction : Down");
} else if(direction == JoyStickClass.STICK_DOWNLEFT) {
    textView5.setText("Direction : Down Left");
} else if(direction == JoyStickClass.STICK_LEFT) {
    textView5.setText("Direction : Left");
} else if(direction == JoyStickClass.STICK_UPLEFT) {
    textView5.setText("Direction : Up Left");
} else if(direction == JoyStickClass.STICK_NONE) {
    textView5.setText("Direction : Center");
}
} else if(arg1.getAction() == MotionEvent.ACTION_UP) {
viewPager.setScrollingEnabled(true);
scrollView.setScrollingEnabled(true);
textView1.setText("X :");
textView2.setText("Y :");
textView3.setText("Angle :");
textView4.setText("Distance :");
textView5.setText("Direction :");
}

 */


