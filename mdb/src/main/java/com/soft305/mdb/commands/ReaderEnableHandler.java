package com.soft305.mdb.commands;

import android.util.Log;

import com.soft305.mdb.MdbCashlessSM;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.EnabledReducer;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;


public class ReaderEnableHandler extends CommandHandler {

    /* vmc cmd: {
          0x01,0x64
        , 0x00,0x01
      };
    */
    byte[] vmcResp = new byte[] {
              0x01, 0x00
        };

    public ReaderEnableHandler(MdbCashlessSM mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public void onDataReceived(VmcInput input) {

        if (input.chunkQueue.getAvailableLength() < 4) {
            return;
        }

        byte[] cmdHead = input.chunkQueue.consume(2);
        Log.d("SetupConfiguration", "cmdHead: " + StringUtil.fromByteArrayToHexString(cmdHead));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(cmdHead, new byte[]{0x01, 0x64})) {
            return;
        }

        byte[] subCmd = input.chunkQueue.consume(2);

        Log.d("SetupConfiguration", "subCmd: " + StringUtil.fromByteArrayToHexString(subCmd));
        input.chunkQueue.getAvailableLength();
        if (!ByteUtil.compare(subCmd, new byte[]{0x00, 0x01})) {
            return;
        }

        mMdbCashless.send(vmcResp);
        input.chunkQueue.empty();
        mMdbCashless.setReducer(new EnabledReducer(mMdbCashless));
    }

}
