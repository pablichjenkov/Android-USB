package com.letmeaccess.usb.host;

import android.hardware.usb.UsbDevice;
import android.support.annotation.NonNull;
import com.letmeaccess.usb.Socket;


/* package */ class UsbHostEmptySocket extends UsbHostSocket {


    UsbHostEmptySocket(@NonNull UsbHostManager usbHostManager, UsbDevice usbDevice, Socket.HostListener listener) {
        super(usbHostManager, usbDevice, listener);
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
