package com.soft305.mdb.commands;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.input.VmcInput;

/**
 * Created by pablo on 5/21/17.
 */
public class EmptyHandler extends CommandHandler {



    public EmptyHandler(Cashless2 mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public void onDataReceived(VmcInput vmcInput) {
        vmcInput.chunkQueue.empty();
    }
}
