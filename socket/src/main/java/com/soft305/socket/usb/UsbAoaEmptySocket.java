package com.soft305.socket.usb;

import android.hardware.usb.UsbAccessory;
import android.support.annotation.NonNull;

/**
 * Created by pablo on 5/20/17.
 */
public class UsbAoaEmptySocket extends UsbAoaSocket {


    UsbAoaEmptySocket(@NonNull UsbAoaManager aoaManager, UsbAccessory accessory) {
        super(aoaManager, accessory);
    }

    @Override
    public void open(@NonNull Listener listener) {

    }

    @Override
    public void openBt(@NonNull BtListener listener) {

    }

    @Override
    public void openBle(@NonNull BleListener listener) {

    }

    @Override
    public void openUsbHost(@NonNull UsbHostListener listener) {

    }

    @Override
    public void openUsbAoa(@NonNull UsbAoaListener listener) {

    }

    @Override
    public void close() {

    }

    @Override
    public void write(byte[] data) {

    }
}
