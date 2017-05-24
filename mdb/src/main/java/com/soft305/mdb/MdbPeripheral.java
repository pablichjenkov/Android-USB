package com.soft305.mdb;

import com.soft305.mdb.input.VmcInput;


public abstract class MdbPeripheral implements StateMachine {


    public MdbManager mMdbManager;


    public MdbPeripheral(MdbManager mdbManager) {
        mMdbManager = mdbManager;
    }

    public void send(byte[] data) {
        mMdbManager.send(data);
    }

    public abstract boolean processCommand(VmcInput vmcInput);

}
