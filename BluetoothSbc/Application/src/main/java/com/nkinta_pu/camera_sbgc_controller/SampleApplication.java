/*
 * Copyright 2014 Sony Corporation
 */

package com.nkinta_pu.camera_sbgc_controller;

import android.app.Application;

import com.nkinta_pu.camera_sbgc_controller.camera.ServerDevice;
import com.nkinta_pu.camera_sbgc_controller.camera.SimpleRemoteApi;
import com.nkinta_pu.camera_sbgc_controller.control.BluetoothService;
import com.nkinta_pu.camera_sbgc_controller.control.CommandDispatcher;
import com.nkinta_pu.camera_sbgc_controller.control.HeadTrackHelper;
import com.nkinta_pu.camera_sbgc_controller.control.SimpleBgcControl;

import java.util.Set;

/**
 * Application class for the sample application.
 */
public class SampleApplication extends Application {

    private SimpleRemoteApi mRemoteApi;

    private Set<String> mSupportedApiSet;

    // private BluetoothService mBluetoothChatService;

    private SimpleBgcControl mSimpleBgcControl;

    private HeadTrackHelper mHeadTrackHelper;

    private CommandDispatcher mCommandDispatcher;

    /**
     * Sets a target ServerDevice object.
     *
     * @param device
     */
    public void setSimpleBgcControl(SimpleBgcControl simpleBgcControl) {
        mSimpleBgcControl = simpleBgcControl;
    }

    public SimpleBgcControl getSimpleBgcControl() {
        return mSimpleBgcControl;
    }

    public void setCommandDispatcher(CommandDispatcher commandDispatcher) {
        mCommandDispatcher = commandDispatcher;
    }

    public CommandDispatcher getCommandDispatcher() {
        return mCommandDispatcher;
    }

    public void setRemoteApi(SimpleRemoteApi remoteApi) {
        mRemoteApi = remoteApi;
    }

    public SimpleRemoteApi getRemoteApi() {
        return mRemoteApi;
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
