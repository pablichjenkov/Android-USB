package com.soft305.mdb.reducer.cashless2;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.Reducer;
import com.soft305.mdb.commands.CommandHandler;
import com.soft305.mdb.commands.ResetHandler;
import com.soft305.mdb.commands.SetupConfigurationHandler;
import com.soft305.mdb.commands.SetupPriceHandler;
import com.soft305.mdb.input.PurchaseInput;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.util.ByteUtil;


public class InactiveReducer extends Reducer<Cashless2> {


    private enum ParsingState {
        WaitCmdHead,
        WaitCmdLen
    }

    private ParsingState mCurParsingState;
    private CommandHandler mCurCommandHandler;
    private final ResetHandler mResetHandler;
    private final SetupConfigurationHandler mSetupConfigurationHandler;
    private final SetupPriceHandler mSetupPriceHandler;

    private int mCurCommandLen = 0;


    public InactiveReducer(Cashless2 cashless2) {
        super(cashless2);
        mCurParsingState = ParsingState.WaitCmdHead;
        mResetHandler = new ResetHandler(this);
        mSetupConfigurationHandler = new SetupConfigurationHandler(this);
        mSetupPriceHandler = new SetupPriceHandler(this);
    }

    @Override
    public void inputPurchase(PurchaseInput purchaseInput) {

    }

    /**
     * This method is a candidate for timeout if the expected cmd length is not buffered in time
     * */
    @Override
    public boolean processVmcInput(VmcInput vmcInput) {

        if (mCurParsingState.equals(ParsingState.WaitCmdHead)) {
            if (vmcInput.chunkQueue.getAvailableLength() < 2) {
                // Wait till we have enough bytes to peek and process CmdHead
                return false;
            }

            byte[] cmdHead = vmcInput.chunkQueue.peek(2);

            if (ByteUtil.compare(cmdHead, CommandHandler.RESET_CMD_HEAD)) {
                mCurCommandHandler = mResetHandler;
                mCurCommandLen = 0;
                mResetHandler.onDataReceived(vmcInput);

            } else if (ByteUtil.compare(cmdHead, CommandHandler.SETUP_CMD_HEAD)) {
                byte[] subCmd = vmcInput.chunkQueue.peek(4);
                if (subCmd != null) {
                    // TODO: Quick Hack to route subCmds, find a better way
                    if (ByteUtil.compare(subCmd, new byte[]{0x01,0x61,0x00,0x00})) {
                        mCurCommandHandler = mSetupConfigurationHandler;
                        mCurCommandLen = 12;

                        if (vmcInput.chunkQueue.getAvailableLength() >= 12) {
                            mSetupConfigurationHandler.onDataReceived(vmcInput);

                        } else {
                            mCurParsingState = ParsingState.WaitCmdLen;
                        }

                    } else if (ByteUtil.compare(subCmd, new byte[]{0x01,0x61,0x00,0x01})) {
                        mCurCommandHandler = mSetupPriceHandler;
                        mCurCommandLen = 12;

                        if (vmcInput.chunkQueue.getAvailableLength() >= 12) {
                            mSetupPriceHandler.onDataReceived(vmcInput);

                        } else {
                            mCurParsingState = ParsingState.WaitCmdLen;
                        }
                    }

                }

            } else if (ByteUtil.compare(cmdHead, CommandHandler.POLL_CMD_HEAD)) {
                // Each command will handle Poll differently so delegate it.
                mCurCommandHandler.onDataReceived(vmcInput);

            } else if (ByteUtil.compare(cmdHead, CommandHandler.READER_CMD_HEAD)) {
                // At this point this Reducer is not used anymore, Candidate for garbage collector
                // since no body should have refernces to it.
                mMdbPeripheral.setReducer(new DisabledReducer(mMdbPeripheral));

            } else {
                // If none of the expected Commands assume is garbage or is not ours
                vmcInput.chunkQueue.consume(2);
            }

        } else if (mCurParsingState.equals(ParsingState.WaitCmdLen)){
            if (vmcInput.chunkQueue.getAvailableLength() >= mCurCommandLen) {
                mCurParsingState = ParsingState.WaitCmdHead;
                mCurCommandHandler.onDataReceived(vmcInput);
            }
        }

        return true;
    }

}
