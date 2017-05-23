package com.soft305.socket.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.util.ArrayMap;
import android.util.Log;
import com.soft305.socket.Socket;
import java.util.Map;

/**
 * Created by pablo on 4/28/17.
 */
public class UsbAoaManager {

    private static final String TAG = "UsbAoaManager";
    private static final String ACTION_USB_PERMISSION = "com.soft305.socket.action.USB_PERMISSION";
    private Context mContext;
    private UsbManager mUsbManager;
    private Listener mListener;
    //private PendingIntent mPermissionIntent;
    private Map<UsbAccessory, UsbAoaSocket> mAccessorySocketMap;
    private UsbAoaSocket mCurSocket;


    public UsbAoaManager (Context context) {
        mContext = context;
        mUsbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        mAccessorySocketMap = new ArrayMap<>();
    }

    public void probe(Listener listener) {
        mListener = listener;
        registerReceiver();
        checkAttachedAccessories();
    }

    public void selectAoa(UsbAccessory accessory){

        if (mUsbManager.hasPermission(accessory)) {
            createAoaSocket(accessory);

        } else {
            synchronized (mUsbAoaReceiver) {

                UsbAoaSocket usbAoaSocket = mAccessorySocketMap.get(accessory);
                if (usbAoaSocket == null) {
                    usbAoaSocket = new UsbAoaEmptySocket(this, accessory);
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
    private void createAoaSocket(UsbAccessory accessory) {
        mCurSocket = new UsbAoaSocket(this, accessory);
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
            mListener.onSelectUsbAoa(accessoryList);
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
        void onSelectUsbAoa(UsbAccessory[] accessoryArray);
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
                            createAoaSocket(accessory);

                        } else {
                            Log.d(TAG, "permission denied for accessory "+ accessory);
                        }
                    }

                }

            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null) {
                    mListener.onSelectUsbAoa(new UsbAccessory[]{accessory});
                }

            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && mAccessorySocketMap.containsKey(accessory)) {
                    UsbAoaSocket aoaSocket = mAccessorySocketMap.get(accessory);
                    if (aoaSocket != null) {
                        aoaSocket.handleAccessoryDetach();
                    }
                }
            }

        }

    };

}
