package com.nkinta_pu.camera_sbgc_controller;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by kanenao on 2015/11/09.
 */
public class ConnectPreferenceActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ConnectPreferenceFragment()).commit();


    }
}
