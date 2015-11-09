package com.nkinta_pu.camera_sbgc_controller;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.nkinta_pu.camera_sbgc_controller.control.DeviceListActivity;

/**
 * Created by kanenao on 2015/11/09.
 */
public class ConnectPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        Preference wifi_ssid_preference = getPreferenceScreen().findPreference("wifi_ssid");
        //  wifi_ssid_preference.setSummary(wifi_ssid_preference.get);

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        String value = pref.getString("wifi_ssid", "defalt");
        wifi_ssid_preference.setSummary((CharSequence) value);
        // EditTextPreference etp;

        wifi_ssid_preference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue != null) {
                    preference.setSummary((CharSequence) newValue);
                    return true;
                }
                return false;
            }
        });

        // wifi_ssid_preference.
        wifi_ssid_preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, 1);

                return true;
            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // connectDevice(data, true);
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor edit = pref.edit();
        edit.putString("wifi_ssid", "fafafa");
        edit.commit();

        Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                Toast.LENGTH_SHORT).show();
    }

}