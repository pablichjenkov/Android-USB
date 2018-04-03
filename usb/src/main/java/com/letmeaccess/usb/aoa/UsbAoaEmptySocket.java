package com.letmeaccess.usb.aoa;

import android.hardware.usb.UsbAccessory;
import android.support.annotation.NonNull;


/* package */ class UsbAoaEmptySocket extends UsbAoaSocket {


    UsbAoaEmptySocket(@NonNull UsbAoaManager aoaManager, @NonNull UsbAccessory accessory
            , @NonNull AccessoryListener listener) {
        super(aoaManager, accessory, listener);
    }

    @Override
    public void open() {
    }

    @Override
    public void close() {
    }

    @Override
    public void write(byte[] data) {
    }

}
