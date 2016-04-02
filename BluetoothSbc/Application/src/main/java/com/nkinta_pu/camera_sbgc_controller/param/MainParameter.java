package com.nkinta_pu.camera_sbgc_controller.param;

import java.util.HashMap;

/**
 * Created by NK on 2016/04/02.
 */

public class MainParameter {

    ControlGamePadParam mControlGamePadParam = new ControlGamePadParam();

    public class ControlGamePadParam {
        float mSpeed;
        float mExpo;
        float mDeadBand;
    }
}
