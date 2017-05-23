package com.soft305.mdb.commands;

import com.soft305.mdb.Reducer;
import com.soft305.mdb.input.VmcInput;


public class ResetHandler extends CommandHandler {

    byte[] vmcResp = new byte[] {0x00, 0x00, 0x01, 0x00}; // JUST RESET Response

    public ResetHandler(Reducer reducer) {
        super(reducer);
    }

    @Override
    public void onDataReceived(VmcInput vmcInput) {
        reducer.sendToVmc(vmcResp);
        vmcInput.chunkQueue.empty();
    }

}
