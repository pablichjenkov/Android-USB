package com.soft305.mdb.commands.device;

import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.util.ByteUtil;

/**
 * This command can only be sent in the Device Enable State and after receive a POLL command from
 * the VMC.
 */
public class BeginSessionHandler extends DeviceCommandHandler {


    /*
    *   Level 1
    *   cmd to vmc: {
    *        0x00,0x03
    *      , 0x00,0xFF
    *      , 0x00,0xFE
    *      , 0x00,0x00 // checksum placeholder
    *   }
    *
    *   Level 2,3
    *   cmd to vmc: {
    *        0x00,0x03
    *      , 0x00,0xFF
    *      , 0x00,0xFE
    *      , 0x00,0x12 // Payment media ID
    *      , 0x00,0x34 // Payment media ID
    *      , 0x00,0x56 // Payment media ID
    *      , 0x00,0x78 // Payment media ID
    *      , 0x00,0x00 // Normal ven card and VMC normal prices
    *      , 0x00,0x0  // No user group
    *      , 0x00,0x0  // No user group
    *      , 0x00,0x00 // checksum placeholder
    *   }
    *
    * */

    public BeginSessionHandler(Cashless2 mdbCashless) {
        super(mdbCashless);
    }

    @Override
    public byte[] buildCommand() {
        byte[] command = new byte[] {
                  0x00, 0x03
                , 0x00,(byte)0xFF
                , 0x00,(byte)0xFE
                , 0x01, 0x00 // checksum placeholder
            };

        return ByteUtil.mdbChkSum(command);
    }

    @Override
    public void onDataReceived(VmcInput input) {

    }

}
