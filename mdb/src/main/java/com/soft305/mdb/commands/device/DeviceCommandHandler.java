package com.soft305.mdb.commands.device;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.commands.CommandHandler;


public abstract class DeviceCommandHandler extends CommandHandler {


    public DeviceCommandHandler(Cashless2 mdbCashless) {
        super(mdbCashless);

    }

    public abstract byte[] buildCommand();

}
