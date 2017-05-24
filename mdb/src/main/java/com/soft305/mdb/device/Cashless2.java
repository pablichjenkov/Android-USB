package com.soft305.mdb.device;

import com.soft305.mdb.MdbManager;
import com.soft305.mdb.MdbPeripheral;
import com.soft305.mdb.Reducer;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.reducer.cashless2.InactiveReducer;


public class Cashless2 extends MdbPeripheral {


    private Reducer mCurReducer;


    public Cashless2(MdbManager mdbManager) {
        super(mdbManager);
        mCurReducer = new InactiveReducer(this);
    }

    /**
     * To be called by internal chain of reducers. The chain of reducer will update the current one.
     * */
    public void setReducer(Reducer newReducer) {
        mCurReducer = newReducer;
    }

    public void send(byte[] data) {
        mMdbManager.send(data);
    }

    @Override
    public boolean processCommand(VmcInput vmcInput) {
        return mCurReducer.processVmcInput(vmcInput);
    }

}
