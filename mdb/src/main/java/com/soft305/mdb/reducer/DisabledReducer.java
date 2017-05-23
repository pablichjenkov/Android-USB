package com.soft305.mdb.reducer;

import com.soft305.mdb.MdbCashlessSM;
import com.soft305.mdb.Reducer;
import com.soft305.mdb.commands.CommandHandler;
import com.soft305.mdb.commands.ResetHandler;
import com.soft305.mdb.input.PurchaseInput;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.util.ByteUtil;


public class DisabledReducer extends Reducer {


    private CommandHandler mCurCommandHandler;
    private final ResetHandler mResetHandler;


    public DisabledReducer(MdbCashlessSM mdbCashlessSM) {
        super(mdbCashlessSM);
        mResetHandler = new ResetHandler(this);
    }

    @Override
    public void inputPurchase(PurchaseInput purchaseInput) {

    }

    @Override
    public void inputVMC(VmcInput vmcInput) {

        // Do the same reducer logic as in InactiveReducer

        if (vmcInput.chunkQueue.getAvailableLength() < 2) {
            // Wait till we have enough bytes to process CmdHead
            return;
        }

        byte[] cmdHead = vmcInput.chunkQueue.peek(2);

        // Route the cmd to the appropriate Handler

        if (ByteUtil.compare(cmdHead, ResetHandler.RESET_CMD_HEAD)) {
            mCurCommandHandler = mResetHandler;
            mResetHandler.onDataReceived(vmcInput);

        } if (ByteUtil.compare(cmdHead, ResetHandler.RESET_CMD_HEAD)) {
            mCurCommandHandler = mResetHandler;
            mResetHandler.onDataReceived(vmcInput);

        } else if (ByteUtil.compare(cmdHead, CommandHandler.POLL_CMD_HEAD)) {
            mCurCommandHandler.onDataReceived(vmcInput);
        }

    }

}
