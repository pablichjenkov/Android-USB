package com.soft305.app.nama;

import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.letmeaccess.usb.Socket;
import com.letmeaccess.usb.aoa.UsbAoaManager;
import com.letmeaccess.usb.host.UsbDeviceConfiguration;
import com.letmeaccess.usb.host.UsbHostManager;

import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private UsbAoaManager mUsbAoaManager;
    private Socket mAoaSocket;
    private UsbHostManager mUsbHostManager;
    private Socket mUsbHostSocket;
    private boolean isUsbSocketOpen;

    private TextView mConsole;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupView();
        setupManagers();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUsbAoaManager != null) {
            mUsbAoaManager.close();
        }
    }

    private void setupView() {
        mConsole = (TextView) findViewById(R.id.txt_mdb_bus);
        mConsole.setMovementMethod(new ScrollingMovementMethod());
    }

    private void cout(final String newLine) {
        mConsole.append("\n");
        mConsole.append(newLine);
    }

    private void setupManagers() {

        //**********************************************
        //****************** USB AOA *******************
        //**********************************************
        mUsbAoaManager = new UsbAoaManager(this);

        UsbAccessory[] attachedAccessories = mUsbAoaManager.getAttachedAccessories();
        if (attachedAccessories == null || attachedAccessories.length <= 0) {
            cout("No attached accessory");
        }

        mUsbAoaManager.probe(new UsbAoaManager.Listener() {
            @Override
            public void onSelectAccessory(UsbAccessory[] accessoryArray) {
                mUsbAoaManager.createSocket(accessoryArray[0], mAccessoryListener);
            }

            @Override
            public void onSocketCreated(Socket socket) {
                mAoaSocket = socket;
                mAoaSocket.open();
            }

        });


        //**********************************************
        //****************** USB HOST ******************
        //**********************************************
        mUsbHostManager = new UsbHostManager(this);
        mUsbHostManager.probe(new UsbHostManager.Listener() {
            @Override
            public void onSelectUsbDevice(Map<String, UsbDevice> usbDeviceMap) {
                //mUsbHostManager.selectDevice();
            }

            @Override
            public void onSocketCreated(Socket socket) {

            }

            @Override
            public UsbDeviceConfiguration onProvideDeviceConfiguration(UsbDevice usbDevice) {
                UsbDeviceConfiguration configuration = new UsbDeviceConfiguration();
                configuration.deviceInterface = usbDevice.getInterface(0);
                configuration.readEndPoint = configuration.deviceInterface.getEndpoint(0);
                configuration.writeEndPoint = configuration.deviceInterface.getEndpoint(1);

                return configuration;
            }
        });

    }

    private Socket.AccessoryListener mAccessoryListener = new Socket.AccessoryListener() {
        @Override
        public void onError(Socket.AccessoryError error) {
            isUsbSocketOpen = false;
            cout("AccessoryListener.onError -> " + error.name());
        }

        @Override
        public byte[] onProvideCloseCommand() {
            return new byte[0];
        }

        @Override
        public void onOpen() {
            isUsbSocketOpen = true;
            cout("AccessoryListener.onOpen()");
            //vmcInput = new VmcInput(new ChunkQueue());
            //mCurPeripheral = new Cashless2(MdbManager.this);
        }

        @Override
        public void onRead(byte[] data) {
            cout("AccessoryListener.onRead() -> " + new String(data));
            if (isUsbSocketOpen) {
                // Feed the Vmc Action queue before being parsed
                //vmcInput.chunkQueue.offer(data);
            }
        }
    };

}
