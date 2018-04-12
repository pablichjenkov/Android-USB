package com.letmeaccess.usb;

/**
 * Created by pablo on 4/28/17.
 */
public interface Socket {

    void open();

    boolean isConnected();

    void write(byte[] data);

    void close();


    interface AccessoryListener {
        void onOpen();
        void onRead(byte[] data);
        void onError(AccessoryError error);
    }

    enum AccessoryError {
        OpenFail
    }

    interface HostListener {
        void onOpen();
        void onRead(byte[] data);
        void onError(HostError error);
    }

    enum HostError {
        NoConfigurationProvided,
        OpenFail,
        ClaimInterfaceFail
    }

}
