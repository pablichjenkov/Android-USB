package com.letmeaccess.usb.aoa;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.ArrayMap;
import android.util.Log;
import com.letmeaccess.usb.Socket;
import java.util.Map;

/**
 * Created by pablo on 4/28/17.
 */
public class UsbAoaManager {

    private static final String TAG = "UsbAoaManager";
    private static final String ACTION_USB_PERMISSION = "com.soft305.socket.action.USB_ACCESSORY_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private Listener mListener;
    private Map<UsbAccessory, UsbAoaSocket> mAccessorySocketMap;
    private UsbAoaSocket mCurSocket;


    // TODO(Pablo): Ask for a Handler to deliver the events on that handler or use BehaviorSubject
    public UsbAoaManager (Context context) {
        mContext = context;
        mAccessorySocketMap = new ArrayMap<>();
    }

    public void probe(Listener listener) {
        mUsbManager = (UsbManager) mContext.getSystemService(Context.USB_SERVICE);
        mListener = listener;
        registerReceiver();
        checkAttachedAccessories();
    }

    public void createSocket(UsbAccessory accessory, Socket.AccessoryListener listener){

        if (mUsbManager.hasPermission(accessory)) {
            createAoaSocket(accessory, listener);

        } else {
            synchronized (mUsbAoaReceiver) {

                UsbAoaSocket usbAoaSocket = mAccessorySocketMap.get(accessory);
                if (usbAoaSocket == null) {
                    usbAoaSocket = new UsbAoaEmptySocket(this, accessory, listener);
                    mAccessorySocketMap.put(accessory, usbAoaSocket);

                } else if (usbAoaSocket.isPendingPermission()) {
                    // Return if the accessory is selected a second time while waiting permission.
                    return;
                }

                usbAoaSocket.setPendingPermission(true);

                PendingIntent permissionIntent = PendingIntent.getBroadcast(mContext
                        , 0, new Intent(ACTION_USB_PERMISSION), 0);

                mUsbManager.requestPermission(accessory, permissionIntent);
            }
        }

    }

    /**
     *   Only call this when the accessory permission has been granted.
     * */
    private void createAoaSocket(UsbAccessory accessory, Socket.AccessoryListener listener) {
        mCurSocket = new UsbAoaSocket(this, accessory, listener);
        mAccessorySocketMap.put(accessory, mCurSocket);
        mListener.onSocketCreated(mCurSocket);
    }

    public void close() {
        mContext.unregisterReceiver(mUsbAoaReceiver);
        for (UsbAoaSocket socket : mAccessorySocketMap.values()) {
            socket.close();
        }
        mAccessorySocketMap.clear();
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);

        mContext.registerReceiver(mUsbAoaReceiver, filter);
    }

    private void checkAttachedAccessories() {
        UsbAccessory[] accessoryList = mUsbManager.getAccessoryList();
        if (accessoryList != null && accessoryList.length > 0) {
            mListener.onSelectAccessory(accessoryList);
        }
    }

    // region: package accessible

    /* package */ void disposeAoaSocket(UsbAoaSocket usbAoaSocket) {
        mAccessorySocketMap.remove(usbAoaSocket.getAccessory());
    }

    /* package */ UsbManager provideManager() {
        return mUsbManager;
    }

    // endregion

    public interface Listener {
        void onSelectAccessory(UsbAccessory[] accessoryArray);
        void onSocketCreated(Socket socket);
    }

    private final BroadcastReceiver mUsbAoaReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (mUsbAoaReceiver) {

                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    UsbAoaSocket usbAoaSocket = mAccessorySocketMap.get(accessory);

                    if (usbAoaSocket != null && usbAoaSocket.isPendingPermission()) {
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            createAoaSocket(accessory, usbAoaSocket.mListener);

                        }
                        else {
                            Log.d(TAG, "permission denied for accessory "+ accessory);
                        }
                    }

                }

            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                checkAttachedAccessories();

            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && mAccessorySocketMap.containsKey(accessory)) {
                    UsbAoaSocket aoaSocket = mAccessorySocketMap.get(accessory);
                    aoaSocket.handleAccessoryDetach();
                }
            }

        }

    };

}
