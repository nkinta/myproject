package com.nkinta_pu.camera_sbgc_controller;

import android.view.View;
import android.widget.Toast;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by kanenao on 2015/09/18.
 */
class CommandInfo {

    private final byte mCommand;
    private final byte[] mData;
    private final String mLabel;

    CommandInfo(String label, byte command, byte[] data) {
        mLabel = label;
        mCommand = command;
        mData = data;
    }

    public String getLabel() {
        return mLabel;
    }

    public byte[] getCommandData() {
        int size = 5 + mData.length;
        // byte[] send = { (byte)0x3E, (byte)0x15, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00};
        byte[] commandData = new byte[size];

        commandData[0] = (byte)0x3E;
        commandData[1] = (byte)mCommand;
        commandData[2] = (byte)mData.length;
        commandData[3] = (byte)(mData.length + mCommand);
        int dataTotal = 0;
        for (int i = 0; i <  mData.length; ++i) {
            commandData[4 + i] = mData[i];
            dataTotal += 0xff & mData[i];
        }
        commandData[4 + mData.length] = (byte)dataTotal;

        return commandData;
    }

}


public class SimpleBgcUtility {
    // Message types sent from the BluetoothChatService Handler

    static int CMD_READ_PARAMS = 82;
    static int CMD_WRITE_PARAMS = 87;
    static int CMD_REALTIME_DATA = 68;
    static int CMD_BOARD_INFO = 86;
    static int CMD_CALIB_ACC = 65;
    static int CMD_CALIB_GYRO = 103;
    static int CMD_CALIB_EXT_GAIN = 71;
    static int CMD_USE_DEFAULTS = 70;
    static int CMD_CALIB_POLES = 80;
    static int CMD_RESET = 114;
    static int CMD_HELPER_DATA = 72;
    static int CMD_CALIB_OFFSET = 79;
    static int CMD_CALIB_BAT = 66;
    static int CMD_MOTORS_ON = 77;
    static int CMD_MOTORS_OFF = 109;
    static int CMD_CONTROL = 67;
    static int CMD_TRIGGER_PIN = 84;
    static int CMD_EXECUTE_MENU = 69;
    static int CMD_GET_ANGLES = 73;
    static int CMD_CONFIRM = 67;
    // Board v3.x only;
    static int CMD_BOARD_INFO_3 = 20;
    static int CMD_READ_PARAMS_3 = 21;
    static int CMD_WRITE_PARAMS_3 = 22;
    static int CMD_REALTIME_DATA_3 = 23;
    static int CMD_REALTIME_DATA_4 = 25;
    static int CMD_SELECT_IMU_3 = 24;
    static int CMD_READ_PROFILE_NAMES = 28;
    static int CMD_WRITE_PROFILE_NAMES = 29;
    static int CMD_QUEUE_PARAMS_INFO_3 = 30;
    static int CMD_SET_ADJ_VARS = 31;
    static int CMD_SAVE_PARAMS_3 = 32;
    static int CMD_READ_PARAMS_EXT = 33;
    static int CMD_WRITE_PARAMS_EXT = 34;
    static int CMD_AUTO_PID = 35;
    static int CMD_SERVO_OUT = 36;
    static int CMD_I2C_WRITE_REG_BUF = 39;
    static int CMD_I2C_READ_REG_BUF = 40;
    static int CMD_WRITE_EXTERNAL_DATA = 41;
    static int CMD_READ_EXTERNAL_DATA = 42;
    static int CMD_READ_ADJ_VARS_CFG = 43;
    static int CMD_WRITE_ADJ_VARS_CFG = 44;
    static int CMD_API_VIRT_CH_CONTROL = 45;
    static int CMD_ADJ_VARS_STATE = 46;
    static int CMD_EEPROM_WRITE = 47;
    static int CMD_EEPROM_READ = 48;
    static int CMD_BOOT_MODE_3 = 51;
    static int CMD_READ_FILE = 53;

    static private byte[] floatDegreeToByte(float v, float oneUnitDeg) {
        short sv = (short) (v * 180 / Math.PI / oneUnitDeg);
        byte result[] =  {(byte) sv, (byte) (sv >> 8)};

        return result;
    }

    static private byte[] floatAngleDegreeToByte(float v) {
        return floatDegreeToByte(v, 0.02197265625f);
    }

    static private byte[] floatSpeedDegreeToByte(float v) {
        return floatDegreeToByte(v, 0.1220740379f);
    }

    static public CommandInfo getControlCommand(float[] speed, float[] angle) {

        if (angle.length > 3) {
            throw new IllegalArgumentException("Angle info must exist 3 element.");
        }

        short x = (short) (angle[0] * 180 / Math.PI / 0.02197265625);
        short y = (short) (angle[1] * 180 / Math.PI / 0.02197265625);
        short z = (short) (angle[2] * 180 / Math.PI / 0.02197265625);

        byte data[] = new byte[13];
        data[0] = (byte) 0x02;
        int offset = 1;
        for (int i = 0; i < 3; ++i) {
            byte byteSpeed[] = floatSpeedDegreeToByte(speed[i]);
            byte byteAngle[] = floatAngleDegreeToByte(angle[i]);
            data[offset + 4 * i + 0] = byteSpeed[0];
            data[offset + 4 * i + 1] = byteSpeed[1];
            data[offset + 4 * i + 2] = byteAngle[0];
            data[offset + 4 * i + 3] = byteAngle[1];
        }

        CommandInfo commandInfo = new CommandInfo("control", (byte)CMD_CONTROL, data);

        return commandInfo;

    }

    static public CommandInfo getGetAngleCommand() {
        byte data[] = {};
        CommandInfo commandInfo = new CommandInfo("control", (byte)CMD_GET_ANGLES, data);

        return commandInfo;

    }

    static public boolean moveAndWait(float[] angle, BluetoothChatService chatService) {

        CommandInfo commandInfo = getControlCommand(new float[] {1.0f, 1.0f, 1.0f}, angle);
        final byte[] commandData = commandInfo.getCommandData();

        byte[] result = chatService.send(commandData);

        return true;

    }

}
