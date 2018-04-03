package com.letmeaccess.usb.host;

import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

public class UsbDeviceConfiguration {
    public UsbInterface deviceInterface;
    public UsbEndpoint writeEndPoint;
    public UsbEndpoint readEndPoint;

}
