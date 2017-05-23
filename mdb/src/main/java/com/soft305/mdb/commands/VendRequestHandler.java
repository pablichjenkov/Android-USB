package com.soft305.mdb.commands;

import android.util.Log;

import com.soft305.mdb.MdbCashlessSM;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.VendingReducer;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class VendRequestHandler extends CommandHandler {

    /*  vmc cmd: {
               0x01,0x63
             , 0x00,0x00  SubCommand Vend Request
             , 0x00,0x00  Scaled Item Price MSB
             , 0x00,0x0A  Scaled Item Price LSB
             , 0x00,0x00  Item Number MSB
             , 0x00,0x1B  Item Number LSB defined by manufacturer, use 0xFFFF if undefined.
         };
    */

    byte[] vendApprovedResp = new byte[] {
              0x00, 0x05
            , 0x00, 0x00 // Scaled Item Price MSB
            , 0x00, 0x0A // Scaled Item Price LSB
            , 0x01, 0x00 // placeholder for checksum
        };

    byte[] vendDeniedResp = new byte[] {
              0x00, 0x06
            , 0x01, 0x06 // fix checksum
    };


    public VendRequestHandler(MdbCashlessSM mdbCashless) {
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
        if (!ByteUtil.compare(subCmd, new byte[]{0x00,0x00})) {
            return;
        }

        byte[] resp = input.chunkQueue.consume(8);

        mMdbCashless.send(ByteUtil.mdbChkSum(vendApprovedResp));
        input.chunkQueue.empty();
        mMdbCashless.setReducer(new VendingReducer(mMdbCashless));

    }

}
