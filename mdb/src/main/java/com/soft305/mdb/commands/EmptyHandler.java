package com.soft305.mdb.commands;

import com.soft305.mdb.MdbCashlessSM;
import com.soft305.mdb.input.VmcInput;

/**
 * Created by pablo on 5/21/17.
 */
public class EmptyHandler extends CommandHandler {



    public EmptyHandler(MdbCashlessSM mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public void onDataReceived(VmcInput vmcInput) {
        vmcInput.chunkQueue.empty();
    }
}
