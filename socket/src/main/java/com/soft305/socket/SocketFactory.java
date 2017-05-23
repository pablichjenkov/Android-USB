package com.soft305.socket;

import com.soft305.socket.usb.UsbAoaManager;

/**
 * Created by pablo on 4/28/17.
 */
public class SocketFactory {


    public void createSocket() {

    }

    public void createUsbAoaSocket(UsbAoaSocketInfo usbAoaSocketInfo) {

    }

    public void createUsbHostSocket() {

    }

    public interface Listener {
        void onSelectSocket();
    }

    public class SocketInfo {
        String name;
    }

    public class UsbAoaSocketInfo {

    }

    public class UsbHostSocketInfo {

    }

}
