package com.letmeaccess.usb.host;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.ArrayMap;
import android.util.Log;
import com.letmeaccess.usb.Socket;
import java.util.Map;

/**
 * Created by pablo on 4/28/17.
 */
// TODO(pablo): Make this class to handle multiple configurations for one device. If Android USB-HOST API allows
// TODO(pablo): Pass a handler to receive event on it.
public class UsbHostManager {

    private static final String TAG = "UsbHostManager";
    private static final String ACTION_USB_PERMISSION = "com.soft305.socket.action.USB_HOST_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private Listener mListener;
    private Map<UsbDevice, UsbHostSocket> mUsbHostSocketMap;
    private UsbHostSocket mCurSocket;


    public UsbHostManager (Context context) {
        mContext = context;
        mUsbHostSocketMap = new ArrayMap<>();
    }

    public void probe(Listener listener) {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mListener = listener;
        registerReceiver();
        checkAttachedDevices();
    }

    public void selectDevice(UsbDevice usbDevice, Socket.HostListener listener){

        if (mUsbManager.hasPermission(usbDevice)) {
            createUsbHostSocket(usbDevice, listener);

        } else {
            synchronized (mUsbHostReceiver) {

                UsbHostSocket usbHostSocket = mUsbHostSocketMap.get(usbDevice);
                if (usbHostSocket == null) {
                    usbHostSocket = new UsbHostEmptySocket(this, usbDevice, listener);
                    mUsbHostSocketMap.put(usbDevice, usbHostSocket);

                } else if (usbHostSocket.isPendingPermission()) {
                    // Return if the device is selected a second time while waiting permission.
                    return;
                }

                usbHostSocket.setPendingPermission(true);

                PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext
                        , 0, new Intent(ACTION_USB_PERMISSION), 0);

                mUsbManager.requestPermission(usbDevice, permissionIntent);
            }
        }

    }

    /**
     *   Only call this when the accessory permission has been granted.
     * */
    private void createUsbHostSocket(UsbDevice usbDevice, Socket.HostListener listener) {
        mCurSocket = new UsbHostSocket(this, usbDevice, listener);
        mUsbHostSocketMap.put(usbDevice, mCurSocket);
        mListener.onSocketCreated(mCurSocket);
    }

    public void close() {
        mContext.unregisterReceiver(mUsbHostReceiver);
        for (UsbHostSocket socket : mUsbHostSocketMap.values()) {
            socket.close();
        }
        mUsbHostSocketMap.clear();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);

        mContext.registerReceiver(mUsbHostReceiver, filter);
    }

    private void checkAttachedDevices() {
        Map<String,UsbDevice> deviceMap = mUsbManager.getDeviceList();
        if (deviceMap != null && deviceMap.size() > 0) {
            mListener.onSelectUsbDevice(deviceMap);
        }
    }

    // region: package accessible

    /* package */ void disposeUsbHostSocket(UsbHostSocket usbHostSocket) {
        mUsbHostSocketMap.remove(usbHostSocket.getUsbDevice());
    }

    /* package */ UsbDeviceConfiguration provideConfigurationForDevice(UsbDevice usbDevice) {
        return mListener.onProvideDeviceConfiguration(usbDevice);
    }

    /* package */ UsbManager provideManager() {
        return mUsbManager;
    }

    // endregion


    public interface Listener {
        void onSelectUsbDevice(Map<String, UsbDevice> usbDeviceMap);
        void onSocketCreated(Socket socket);
        UsbDeviceConfiguration onProvideDeviceConfiguration(UsbDevice usbDevice);
    }

    private final BroadcastReceiver mUsbHostReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (mUsbHostReceiver) {

                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    UsbHostSocket usbHostSocket = mUsbHostSocketMap.get(usbDevice);

                    if (usbHostSocket != null && usbHostSocket.isPendingPermission()) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            createUsbHostSocket(usbDevice, usbHostSocket.mListener);

                        } else {
                            Log.d(TAG, "permission denied for device "+ usbDevice);
                        }
                    }

                }

            } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                checkAttachedDevices();

            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (usbDevice != null && mUsbHostSocketMap.containsKey(usbDevice)) {
                    UsbHostSocket hostSocket = mUsbHostSocketMap.get(usbDevice);
                    hostSocket.handleUsbDeviceDetach();
                }
            }

        }

    };

}
