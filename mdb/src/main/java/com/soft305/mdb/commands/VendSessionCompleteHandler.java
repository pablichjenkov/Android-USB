package com.soft305.mdb.commands;

import android.util.Log;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.cashless2.EnabledReducer;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class VendSessionCompleteHandler extends CommandHandler {

    /*  vmc cmd: {
               0x01,0x63
             , 0x00,0x04  SubCommand Session Complete
             , 0x00,0x00  Scaled Item Price MSB
             , 0x00,0x0A  Scaled Item Price LSB
             , 0x00,0x00  Item Number MSB
             , 0x00,0x1B  Item Number LSB defined by manufacturer, use 0xFFFF if undefined.
         };
    */

    byte[] vendResp = new byte[] {
              0x00, 0x07
            , 0x01, 0x07 // fix checksum
    };


    public VendSessionCompleteHandler(Cashless2 mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public void onDataReceived(VmcInput input) {

        if (input.chunkQueue.getAvailableLength() < 12) {
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
        if (!ByteUtil.compare(subCmd, new byte[]{0x00,0x04})) {
            return;
        }

        byte[] resp = input.chunkQueue.consume(8);

        mMdbCashless.send(ByteUtil.mdbChkSum(vendResp));
        input.chunkQueue.empty();
        mMdbCashless.setReducer(new EnabledReducer(mMdbCashless));

    }

}
