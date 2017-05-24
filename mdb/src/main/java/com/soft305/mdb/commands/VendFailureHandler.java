package com.soft305.mdb.commands;

import android.util.Log;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.cashless2.EnabledReducer;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class VendFailureHandler extends CommandHandler {

    /*  vmc cmd: {
               0x01,0x63
             , 0x00,0x03  SubCommand Vend Failure
             , 0x00,0x00  Item Number MSB
             , 0x00,0x1B  Item Number LSB defined by manufacturer.
         };
    */

    byte[] ackResp = new byte[] {
            0x01, 0x00
    };


    public VendFailureHandler(Cashless2 mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public void onDataReceived(VmcInput input) {

        if (input.chunkQueue.getAvailableLength() < 8) {
            return;
        }

        byte[] cmdHead = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "cmdHead: " + StringUtil.fromByteArrayToHexString(cmdHead));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(cmdHead, new byte[]{0x01,0x63})) {
            return;
        }

        byte[] subCmd = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "subCmd: " + StringUtil.fromByteArrayToHexString(subCmd));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(subCmd, new byte[]{0x00,0x03})) {
            return;
        }

        byte[] resp = input.chunkQueue.consume(4);

        mMdbCashless.send(ackResp);
        input.chunkQueue.empty();
        mMdbCashless.setReducer(new EnabledReducer(mMdbCashless));

    }

}
