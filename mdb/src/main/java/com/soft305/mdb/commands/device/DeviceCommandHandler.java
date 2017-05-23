package com.soft305.mdb.commands.device;

import com.soft305.mdb.MdbCashlessSM;
import com.soft305.mdb.commands.CommandHandler;


public abstract class DeviceCommandHandler extends CommandHandler {


    public DeviceCommandHandler(MdbCashlessSM mdbCashless) {
        super(mdbCashless);

    }

    public abstract byte[] buildCommand();

}
