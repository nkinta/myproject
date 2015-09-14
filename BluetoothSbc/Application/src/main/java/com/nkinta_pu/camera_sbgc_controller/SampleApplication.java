/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import android.app.Application;

import java.util.Set;

/**
 * Application class for the sample application.
 */
public class SampleApplication extends Application {

    private ServerDevice mTargetDevice;

    private SimpleRemoteApi mRemoteApi;

    private Set<String> mSupportedApiSet;

    private BluetoothChatService mBluetoothChatService;

    /**
     * Sets a target ServerDevice object.
     *
     * @param device
     */
    public void setBluetoothChatService(BluetoothChatService service) {
        mBluetoothChatService = service;
    }

    public BluetoothChatService getBluetoothChatService() {
        return mBluetoothChatService;
    }

    public void setTargetServerDevice(ServerDevice device) {
        mTargetDevice = device;
    }

    public ServerDevice getTargetServerDevice() {
        return mTargetDevice;
    }

    public void setRemoteApi(SimpleRemoteApi remoteApi) {
        mRemoteApi = remoteApi;
    }

    public SimpleRemoteApi getRemoteApi() {
        return mRemoteApi;
    }

    public void setSupportedApiList(Set<String> apiList) {
        mSupportedApiSet = apiList;
    }

    public Set<String> getSupportedApiList() {
        return mSupportedApiSet;
    }
}