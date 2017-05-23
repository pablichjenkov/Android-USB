package com.soft305.mdb.commands;

import android.util.Log;

import com.soft305.mdb.Reducer;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class SetupConfigurationHandler extends CommandHandler {

    /* vmc cmd: {
          0x01,0x61
        , 0x00,0x00
        , 0x00,0x01
        , 0x00,0x00
        , 0x00,0x00
        , 0x00,0x00
      };
    */

    byte[] vmcResp = new byte[] {
              0x00, 0x01
            , 0x00, 0x01
            , 0x00, 0x18 // MSB Country Code BCD encoded
            , 0x00, 0x40 // LSB Country Code BCD encoded
            , 0x00, (byte)25 // Scale factor
            , 0x00, 0x02 // Decimal Places
            , 0x00, 0x0A // Scale factor
            , 0x00, 0x00 // Miscellanea
            , 0x01, 0x00 // Will be filled with CHK_SUM
        };

    public SetupConfigurationHandler(Reducer reducer) {
        super(reducer);
    }

    @Override
    public void onDataReceived(VmcInput input) {

        if (input.chunkQueue.getAvailableLength() < 12) {
            return;
        }

        byte[] cmdHead = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "cmdHead: " + StringUtil.fromByteArrayToHexString(cmdHead));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(cmdHead, new byte[]{0x01,0x61})) {
            return;
        }

        byte[] subCmd = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "subCmd: " + StringUtil.fromByteArrayToHexString(subCmd));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(subCmd, new byte[]{0x00,0x00})) {
            return;
        }

        byte[] resp = input.chunkQueue.consume(8);
        input.chunkQueue.getAvailableLength();

        reducer.sendToVmc(ByteUtil.mdbChkSum(vmcResp));
        input.chunkQueue.empty();

    }

}
