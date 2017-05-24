package com.soft305.mdb.device;


import java.util.ArrayList;
import java.util.List;

public class PeripheralHelper {


    List<AddressRange> mAddressRangeList;

    public PeripheralHelper() {
        mAddressRangeList = new ArrayList<>(Peripheral.values().length);
        mAddressRangeList.add(new CoinAcceptorAddressRange());
        mAddressRangeList.add(new Cashless1AddressRange());
        mAddressRangeList.add(new DisplayAddressRange());
        mAddressRangeList.add(new BillValidatorAddressRange());
        mAddressRangeList.add(new Cashless2AddressRange());
    }

    public Peripheral getPeripheralByCmdAddress(byte cmdAddressByte) {

        int cmdAddressInt = cmdAddressByte & 0xFF;

        for (AddressRange addressRange : mAddressRangeList) {
            Peripheral peripheral = addressRange.resolve(cmdAddressInt);
            if (peripheral != null) {
                return peripheral;
            }

        }

        return null;
    }


    private interface AddressRange {
        Peripheral resolve(int cmdAddress);
    }

    private class CoinAcceptorAddressRange implements AddressRange {

        @Override
        public Peripheral resolve(int cmdAddress) {
            Peripheral result = null;
            if ( 0x08 <= cmdAddress && cmdAddress <= 0x0f) { // 08 - 0F
                result = Peripheral.CoinAcceptor;
            }
            return result;
        }

    }

    private class Cashless1AddressRange implements AddressRange {

        @Override
        public Peripheral resolve(int cmdAddress) {
            Peripheral result = null;
            if ( 0x10 <= cmdAddress && cmdAddress <= 0x17) { // 10 - 17
                result = Peripheral.Cashless1;
            }
            return result;
        }

    }

    private class DisplayAddressRange implements AddressRange {

        @Override
        public Peripheral resolve(int cmdAddress) {
            Peripheral result = null;
            if ( 0x20 <= cmdAddress && cmdAddress <= 0x27) { // 20 - 27
                result = Peripheral.Display;
            }
            return result;
        }

    }

    private class BillValidatorAddressRange implements AddressRange {

        @Override
        public Peripheral resolve(int cmdAddress) {
            Peripheral result = null;
            if ( 0x30 <= cmdAddress && cmdAddress <= 0x37) { // 30 - 37
                result = Peripheral.BillValidator;
            }
            return result;
        }

    }

    private class Cashless2AddressRange implements AddressRange {

        @Override
        public Peripheral resolve(int cmdAddress) {
            Peripheral result = null;
            if ( 0x60 <= cmdAddress && cmdAddress <= 0x67) { // 60 - 67
                result = Peripheral.Cashless2;
            }
            return result;
        }

    }

}
