package com.example.android.bluetoothchat;

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
                return new Fragment();
            case 1:
                return new CameraRemoteFragment();
            default:
                return new BluetoothChatFragment();
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
