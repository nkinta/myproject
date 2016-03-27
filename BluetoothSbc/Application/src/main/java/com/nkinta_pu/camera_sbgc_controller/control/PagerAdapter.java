package com.nkinta_pu.camera_sbgc_controller.control;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.MotionEvent;

public class PagerAdapter extends FragmentStatePagerAdapter {

    FragmentManager mFm;

    public PagerAdapter(FragmentManager fm) {
        super(fm);
        mFm = fm;
    }


    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                // FragmentTransaction transaction = mFm.beginTransaction();
                // CameraFragment fragment = new CameraFragment();
                // transaction.replace(R.id.sample_content_fragment, fragment);
                // transaction.commit();
                // return new CameraDeviceFragment();
                return new HeadTrackFragment();
            case 1:
                return new AutoShutterFragment();
            case 2:
                return new GamePadFragment();
            case 3:
                return new VirtualGamePadFragment();
            default:
                return new MessageFragment();

        }
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }

}
