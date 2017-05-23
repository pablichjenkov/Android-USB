package com.soft305.mdb.commands;

import android.util.Log;
import com.soft305.mdb.Reducer;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.DisabledReducer;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class SetupPriceHandler extends CommandHandler {

    /* vmc cmd: {
          0x01,0x61
        , 0x00,0x01
        , 0x00,0x00 // Maximum Price MSB
        , 0x00,0x01 // Maximum Price LSB (1)
        , 0x00,0x00 // Minimum Price MSB
        , 0x00,0xFA // Minimum Price LSB (250)
      };
    */
    byte[] vmcResp = new byte[] {
              0x01, 0x00
        };

    public SetupPriceHandler(Reducer reducer) {
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
        if (!ByteUtil.compare(cmdHead, new byte[]{0x01, 0x61})) {
            return;
        }

        byte[] subCmd = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "subCmd: " + StringUtil.fromByteArrayToHexString(subCmd));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(subCmd, new byte[]{0x00, 0x01})) {
            return;
        }

        byte[] resp = input.chunkQueue.consume(8);
        // resp contains max/min price details

        reducer.sendToVmc(vmcResp);
        input.chunkQueue.empty();
    }

}
