package com.soft305.mdb;


import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.input.PurchaseInput;
import com.soft305.mdb.input.VmcInput;

public abstract class Reducer<T extends MdbPeripheral> {

    protected T mMdbPeripheral;

    public Reducer (T mdbPeripheral) {
        this.mMdbPeripheral = mdbPeripheral;
    }

    /**
     * To be called by command handler to respond to vmc events
     * */
    public void sendToVmc(byte[] outboundData) {
        mMdbPeripheral.send(outboundData);
    }

    public abstract void inputPurchase(PurchaseInput purchaseInput);
    public abstract boolean processVmcInput(VmcInput vmcInput);
}
