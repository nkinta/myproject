package com.nkinta_pu.camera_sbgc_controller;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

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
                return new MessageFragment();
            case 1:
                return new HeadTrackFragment();
            default:
                return new AutoShutterFragment();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + position;
    }
}
