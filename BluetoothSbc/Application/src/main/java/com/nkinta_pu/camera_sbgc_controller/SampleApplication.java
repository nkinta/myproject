/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import android.app.Application;

import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Application class for the sample application.
 */
public class SampleApplication extends Application {

    private ServerDevice mTargetDevice;

    private SimpleRemoteApi mRemoteApi;

    private Set<String> mSupportedApiSet;

    private BluetoothChatService mBluetoothChatService;

    private HeadTrackHelper mHeadTrackHelper;

    private ArrayBlockingQueue<byte[]> mBluetoothBlockingQueue = new ArrayBlockingQueue<byte[]>(10);

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

    public ArrayBlockingQueue<byte[]> getBluetoothBlockingQueue() {
        return mBluetoothBlockingQueue;
    }

    public void setBluetoothBlockingQueue(ArrayBlockingQueue<byte[]> queue) {
        mBluetoothBlockingQueue = queue;
    }

    public void setHeadTrackHelper(HeadTrackHelper headTrackHelper) {
        mHeadTrackHelper = headTrackHelper;
    }

    public HeadTrackHelper getHeadTrackHelper() {
        return mHeadTrackHelper;
    }

    public void setSupportedApiList(Set<String> apiList) {
        mSupportedApiSet = apiList;
    }

    public Set<String> getSupportedApiList() {
        return mSupportedApiSet;
    }
}
