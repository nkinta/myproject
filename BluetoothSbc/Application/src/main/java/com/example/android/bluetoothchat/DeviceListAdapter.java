package com.example.android.bluetoothchat;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NK on 2015/09/04.
 */
class DeviceListAdapter extends BaseAdapter {

    private final List<ServerDevice> mDeviceList;

    private final LayoutInflater mInflater;

    public DeviceListAdapter(Context context) {
        mDeviceList = new ArrayList<ServerDevice>();
        mInflater = LayoutInflater.from(context);
    }

    public void addDevice(ServerDevice device) {
        mDeviceList.add(device);
        notifyDataSetChanged();
    }

    public void clearDevices() {
        mDeviceList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0; // not fine
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = (TextView) convertView;
        if (textView == null) {
            textView = (TextView) mInflater.inflate(R.layout.device_list_item, parent, false);
        }
        ServerDevice device = (ServerDevice) getItem(position);
        ServerDevice.ApiService apiService = device.getApiService("camera");
        String endpointUrl = null;
        if (apiService != null) {
            endpointUrl = apiService.getEndpointUrl();
        }

        // Label
        String htmlLabel =
                String.format("%s ", device.getFriendlyName()) //
                        + String.format(//
                        "<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", //
                        endpointUrl);
        textView.setText(Html.fromHtml(htmlLabel));

        return textView;
    }
}