package com.nkinta_pu.camera_sbgc_controller.control;

/**
 * Created by kanenao on 2015/09/18.
 */

public class SimpleBgcControl {
    // Message types sent from the BluetoothService Handler

    static final int PARAM_P_ROLL = 0;
    static final int PARAM_P_PITCH = 1;
    static final int PARAM_P_YAW = 2;
    static final int PARAM_I_ROLL = 3;
    static final int PARAM_I_PITCH = 4;
    static final int PARAM_I_YAW = 5;
    static final int PARAM_D_ROLL = 6;
    static final int PARAM_D_PITCH = 7;
    static final int PARAM_D_YAW = 8;
    static final int PARAM_POWER_ROLL = 9;
    static final int PARAM_POWER_PITCH = 10;
    static final int PARAM_POWER_YAW = 11;
    static final int PARAM_ACC_LIMITER = 12;
    static final int PARAM_FOLLOW_SPEED_ROLL = 13;
    static final int PARAM_FOLLOW_SPEED_PITCH = 14;
    static final int PARAM_FOLLOW_SPEED_YAW = 15;
    static final int PARAM_FOLLOW_LPF_ROLL = 16;
    static final int PARAM_FOLLOW_LPF_PITCH = 17;
    static final int PARAM_FOLLOW_LPF_YAW = 18;
    static final int PARAM_RC_SPEED_ROLL = 19;
    static final int PARAM_RC_SPEED_PITCH = 20;
    static final int PARAM_RC_SPEED_YAW = 21;
    static final int PARAM_RC_LPF_ROLL = 22;
    static final int PARAM_RC_LPF_PITCH = 23;
    static final int PARAM_RC_LPF_YAW = 24;
    static final int PARAM_RC_TRIM_ROLL = 25;
    static final int PARAM_RC_TRIM_PITCH = 26;
    static final int PARAM_RC_TRIM_YAW = 27;
    static final int PARAM_RC_DEADBAND = 28;
    static final int PARAM_RC_EXPO_RATE = 29;
    static final int PARAM_FOLLOW_MODE = 30;
    static final int PARAM_FOLLOW_YAW = 31;
    static final int PARAM_FOLLOW_DEADBAND = 32;
    static final int PARAM_FOLLOW_EXPO_RATE = 33;
    static final int PARAM_FOLLOW_ROLL_MIX_START = 34;
    static final int PARAM_FOLLOW_ROLL_MIX_RANGE = 35;
    static final int PARAM_GYRO_TRUST = 36;
    static final int PARAM_FRAME_HEADING_ANGLE = 37;
    static final int PARAM_GYRO_HEADING_CORRECTION = 38;

    static final int CMD_READ_PARAMS = 82;
    static final int CMD_WRITE_PARAMS = 87;
    static final int CMD_REALTIME_DATA = 68;
    static final int CMD_BOARD_INFO = 86;
    static final int CMD_CALIB_ACC = 65;
    static final int CMD_CALIB_GYRO = 103;
    static final int CMD_CALIB_EXT_GAIN = 71;
    static final int CMD_USE_DEFAULTS = 70;
    static final int CMD_CALIB_POLES = 80;
    static final int CMD_RESET = 114;
    static final int CMD_HELPER_DATA = 72;
    static final int CMD_CALIB_OFFSET = 79;
    static final int CMD_CALIB_BAT = 66;
    static final int CMD_MOTORS_ON = 77;
    static final int CMD_MOTORS_OFF = 109;
    static final int CMD_CONTROL = 67;
    static final int CMD_TRIGGER_PIN = 84;
    static final int CMD_EXECUTE_MENU = 69;
    static final int CMD_GET_ANGLES = 73;
    static final int CMD_CONFIRM = 67;
    // Board v3.x only;
    static final int CMD_BOARD_INFO_3 = 20;
    static final int CMD_READ_PARAMS_3 = 21;
    static final int CMD_WRITE_PARAMS_3 = 22;
    static final int CMD_REALTIME_DATA_3 = 23;
    static final int CMD_REALTIME_DATA_4 = 25;
    static final int CMD_SELECT_IMU_3 = 24;
    static final int CMD_READ_PROFILE_NAMES = 28;
    static final int CMD_WRITE_PROFILE_NAMES = 29;
    static final int CMD_QUEUE_PARAMS_INFO_3 = 30;
    static final int CMD_SET_ADJ_VARS_VAL = 31;
    static final int CMD_SAVE_PARAMS_3 = 32;
    static final int CMD_READ_PARAMS_EXT = 33;
    static final int CMD_WRITE_PARAMS_EXT = 34;
    static final int CMD_AUTO_PID = 35;
    static final int CMD_SERVO_OUT = 36;
    static final int CMD_I2C_WRITE_REG_BUF = 39;
    static final int CMD_I2C_READ_REG_BUF = 40;
    static final int CMD_WRITE_EXTERNAL_DATA = 41;
    static final int CMD_READ_EXTERNAL_DATA = 42;
    static final int CMD_READ_ADJ_VARS_CFG = 43;
    static final int CMD_WRITE_ADJ_VARS_CFG = 44;
    static final int CMD_API_VIRT_CH_CONTROL = 45;
    static final int CMD_ADJ_VARS_STATE = 46;
    static final int CMD_EEPROM_WRITE = 47;
    static final int CMD_EEPROM_READ = 48;
    static final int CMD_BOOT_MODE_3 = 51;
    static final int CMD_READ_FILE = 53;

    static final byte MODE_NO_CONTROL = 0;
    static final byte MODE_SPEED = 1;
    static final byte MODE_ANGLE = 2;
    static final byte MODE_SPEED_ANGLE = 3;
    static final byte MODE_RC = 4;
    static final byte MODE_ANGLE_REL_FRAME = 5;

    static final float ANGLE_UNIT = 0.02197265625f;
    static final float ANGLE_SPEED_UNIT = 0.1220740379f;

    private final BluetoothService mBluetoothService;
    private final CommandDispatcher mCommandDispatcher;

    public SimpleBgcControl(BluetoothService bluetoothService) {
        mBluetoothService = bluetoothService;
        mCommandDispatcher = new CommandDispatcher();
        mCommandDispatcher.start();
    }

    public BluetoothService getBluetoothService() {
        return mBluetoothService;
    }

    public CommandDispatcher getCommandDispatcher() {
        return mCommandDispatcher;
    }

    static public byte[] getCommandData(byte command, byte[] data) {
        int size = 5 + data.length;
        // byte[] send = { (byte)0x3E, (byte)0x15, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00};
        byte[] commandData = new byte[size];

        commandData[0] = (byte)0x3E;
        commandData[1] = command;
        commandData[2] = (byte)data.length;
        commandData[3] = (byte)(data.length + command);
        int dataTotal = 0;
        for (int i = 0; i <  data.length; ++i) {
            commandData[4 + i] = data[i];
            dataTotal += 0xff & data[i];
        }
        commandData[4 + data.length] = (byte)dataTotal;

        return commandData;
    }
    static private byte[] floatDegreeToByte(float v, float oneUnitDeg) {
        short sv = (short) (v * 180 / Math.PI / oneUnitDeg);
        byte result[] =  {(byte) sv, (byte) (sv >> 8)};

        return result;
    }

    static private byte[] floatAngleDegreeToByte(float v) {
        return floatDegreeToByte(v, ANGLE_UNIT);
    }

    static private byte[] floatSpeedDegreeToByte(float v) {
        return floatDegreeToByte(v, ANGLE_SPEED_UNIT);
    }

    static public byte[] getControlCommand(byte controlMode, float[] speed, float[] angle) {

        if (angle.length > 3) {
            throw new IllegalArgumentException("Angle info must exist 3 element.");
        }

        byte data[] = new byte[13];
        data[0] = controlMode;
        int offset = 1;
        for (int i = 0; i < 3; ++i) {
            byte byteSpeed[] = floatSpeedDegreeToByte(speed[i]);
            byte byteAngle[] = floatAngleDegreeToByte(angle[i]);
            data[offset + 4 * i + 0] = byteSpeed[0];
            data[offset + 4 * i + 1] = byteSpeed[1];
            data[offset + 4 * i + 2] = byteAngle[0];
            data[offset + 4 * i + 3] = byteAngle[1];
        }

        byte[] commandData = getCommandData((byte) CMD_CONTROL, data);

        return commandData;

    }

    static public float getFloatFromByte(byte dataList[], float oneUnitDeg) {

        int temp = 0;
        for (int i = 0; i < dataList.length; ++i) {
            int add = 0;
            if (i < dataList.length - 1) {
                add = ((dataList[0] & 0xff)) << i * 8;
            }
            else {
                add = ((int) dataList[0]) << i * 8;
            }
            temp += add;
        }
        float result = (float) temp * oneUnitDeg;

        return result;
    }

    public void getProfile(int index) {
        byte[] data = {(byte)index};
        mBluetoothService.send(getCommandData((byte) CMD_READ_PARAMS_3, data));
    }

    public void calibrationAcc(int imuIndex) {
        byte[] data = new byte[12];
        data[0] = (byte)imuIndex;
        data[1] = (byte)1;
        mBluetoothService.send(getCommandData((byte) CMD_CALIB_ACC, data));
    }

    public void calibrationGyro(int imuIndex) {
        byte[] data = new byte[12];
        data[0] = (byte)imuIndex;
        data[1] = (byte)1;
        mBluetoothService.send(getCommandData((byte) CMD_CALIB_GYRO, data));
    }

    public void followPitchRoll(boolean flag) {
        byte[] data = new byte[6];
        data[0] = (byte)1;
        data[1] = (byte)PARAM_FOLLOW_MODE;
        if (flag) {
            data[2] = (byte)2;
        }
        else {
            data[2] = (byte)1;
        }
        mBluetoothService.send(getCommandData((byte) CMD_SET_ADJ_VARS_VAL, data));
    }

    public void followYaw(boolean flag) {
        byte[] data = new byte[6];
        data[0] = (byte)1;
        data[1] = (byte)PARAM_FOLLOW_YAW;
        if (flag) {
            data[2] = (byte)1;
        }
        else {
            data[2] = (byte)0;
        }
        mBluetoothService.send(getCommandData((byte) CMD_SET_ADJ_VARS_VAL, data));
    }

    public synchronized void moveSync(float[] speed, float[] angle) {

        final byte[] commandData = getControlCommand(MODE_ANGLE, speed, angle);

        byte[] result = mBluetoothService.sendSync(commandData);

        return;
    }

    public void move(float[] speed, float[] angle) {

        final byte[] commandData = getControlCommand(MODE_ANGLE, speed, angle);

        mBluetoothService.send(commandData);

        return;
    }

    public void setSpeed(float[] speed) {

        final byte[] commandData = getControlCommand(MODE_SPEED, speed, new float[] {0f, 0f, 0f});

        mBluetoothService.send(commandData);

        return;
    }

    public synchronized float[] getAngleRcSpeed() {
        byte[] data = {};
        final byte[] result =  mBluetoothService.sendSync(getCommandData((byte) CMD_GET_ANGLES, data));
        if (result == null) {
            return null;
        }

        float[] imuAngle = {0f, 0f, 0f};
        float[] rcTargetAngle = {0f, 0f, 0f};
        float[] rcSpeed = {0f, 0f, 0f};

        for (int i = 0; i < 3; ++i) {
            imuAngle[i] = getFloatFromByte(new byte[]{result[6 * i + 0], result[6 * i + 1]}, ANGLE_UNIT);
            rcTargetAngle[i] = getFloatFromByte(new byte[]{result[6 * i + 2], result[6 * i + 2]}, ANGLE_UNIT);
            rcSpeed[i] = getFloatFromByte(new byte[]{result[6 * i + 4], result[6 * i + 5]}, ANGLE_SPEED_UNIT);
        }
        return rcSpeed;
    }

    public synchronized void waitUntilStop() {

        while (true) {
            /*
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */

            float[] rcSpeed = getAngleRcSpeed();
            if (rcSpeed == null) {
                continue;
            }

            float maxValue = 0;
            for (float v : rcSpeed) {
                if (maxValue < Math.abs(v)) {
                    maxValue = Math.abs(v);
                }
            }
            if (maxValue < 0.001) {
                return;
            }
        }
    }
}
