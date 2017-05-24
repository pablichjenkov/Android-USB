package com.soft305.mdb.commands;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.Reducer;
import com.soft305.mdb.input.VmcInput;


public abstract class CommandHandler {

    public static final byte[] RESET_CMD_HEAD   = new byte[] {0x01, 0x60};
    public static final byte[] SETUP_CMD_HEAD   = new byte[] {0x01, 0x61};
    public static final byte[] POLL_CMD_HEAD    = new byte[] {0x01, 0x62};
    public static final byte[] VEND_CMD_HEAD    = new byte[] {0x01, 0x63};
    public static final byte[] READER_CMD_HEAD  = new byte[] {0x01, 0x64};
    public static final byte[] REVAL_CMD_HEAD   = new byte[] {0x01, 0x65};
    public static final byte[] EXPAN_CMD_HEAD   = new byte[] {0x01, 0x67};

    protected Cashless2 mMdbCashless;
    protected Reducer reducer;

    // TODO: remove this constructor
    public CommandHandler(Cashless2 mdbCashless) {

    }

    public CommandHandler(Reducer reducer) {
        this.reducer = reducer;
    }

    // TODO: Change this method name
    public abstract void onDataReceived(VmcInput vmcInput);

}
