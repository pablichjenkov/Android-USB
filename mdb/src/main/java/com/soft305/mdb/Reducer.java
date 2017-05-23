package com.soft305.mdb;


import com.soft305.mdb.input.PurchaseInput;
import com.soft305.mdb.input.VmcInput;

public abstract class Reducer {

    protected MdbCashlessSM mdbCashlessSM;

    public Reducer (MdbCashlessSM mdbCashlessSM) {
        this.mdbCashlessSM  = mdbCashlessSM;
    }

    /**
     * To be called by command handler to respond to vmc events
     * */
    public void sendToVmc(byte[] outboundData) {
        mdbCashlessSM.send(outboundData);
    }

    public abstract void inputPurchase(PurchaseInput purchaseInput);
    public abstract void inputVMC(VmcInput vmcInput);
}
