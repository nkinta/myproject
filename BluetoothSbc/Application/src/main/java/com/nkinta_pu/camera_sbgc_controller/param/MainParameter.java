package com.nkinta_pu.camera_sbgc_controller.param;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by NK on 2016/04/02.
 */

public class MainParameter implements Serializable {

    public static class FloatValue implements Serializable  {
        public FloatValue(float tempValue) {
            value = tempValue;
        }
        public float value = 0.0f;
    }

    public static class IntValue implements Serializable {
        public IntValue(int tempValue) {
            value = tempValue;
        }
        public int value = 0;
    }

    public static class BooleanValue implements Serializable {
        public BooleanValue(boolean tempValue) {
            value = tempValue;
        }
        public boolean value = true;
    }

    public ControlGamePadParam mControlGamePadParam = new ControlGamePadParam();
    public AutoShutterParam mAutoShutterParam = new AutoShutterParam();
    public HeadTrackParam mHeadTrackParam = new HeadTrackParam();
    public ControlVirtualGamePadParam mControlVirtualGamePadParam = new ControlVirtualGamePadParam();

    public class ControlGamePadParam implements Serializable {
        public FloatValue mSpeed = new FloatValue(1.0f);
        public FloatValue mExpo = new FloatValue(2.0f);
        public FloatValue mDeadBand = new FloatValue(0.1f);

        public IntValue mRollId = new IntValue(2);
        public IntValue mPitchId = new IntValue(1);
        public IntValue mYawId = new IntValue(0);

        public BooleanValue mRollInverseFlag = new BooleanValue(false);
        public BooleanValue mPitchInverseFlag = new BooleanValue(false);
        public BooleanValue mYawInverseFlag = new BooleanValue(false);
    }

    public class AutoShutterParam implements Serializable {
        public FloatValue mSpeed = new FloatValue(1.0f);
    }

    public class ControlVirtualGamePadParam implements Serializable {
        public FloatValue mSpeed = new FloatValue(1.0f);
        public FloatValue mExpo = new FloatValue(2.0f);
        public FloatValue mDeadBand = new FloatValue(0.1f);
    }

    public class HeadTrackParam implements Serializable {
        public FloatValue mSpeed = new FloatValue(1.0f);
    }

}
