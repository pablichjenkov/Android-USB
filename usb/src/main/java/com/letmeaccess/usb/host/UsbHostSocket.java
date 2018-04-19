package com.letmeaccess.usb.host;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import com.letmeaccess.usb.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


/* package */ class UsbHostSocket implements Socket {

    private static final String TAG = "UsbHostSocket";
    private UsbHostManager mUsbHostManager;
    private UsbDevice mUsbDevice;
    protected Socket.HostListener mListener;
    private UsbDeviceConnection mDeviceConnection;
    private UsbInterface mUsbInterface;
    private UsbEndpoint mReadEndPoint;
    private UsbEndpoint mWriteEndPoint;
    private ReceiverThread mReceiverThread;
    private SenderThread mSenderThread;
    private RequestRouterThread mRequestRouterThread;
    private BlockingQueue<UsbRequest> mWriteRequestQueue = new LinkedBlockingQueue<>();
    private UsbRequest mReadRequest = new UsbRequest();
    private ByteBuffer mInputByteBuffer;
    private final Object mReadRequestMonitor = new Object();
    private boolean mIsReadInQueue;
    private boolean mIsPendingPermission;
    private boolean mIsConnected;
    private boolean mIsDisconnecting;
    private boolean isError;

    /* package */ UsbHostSocket(@NonNull UsbHostManager usbHostManager
            , @NonNull UsbDevice usbDevice, @NonNull Socket.HostListener listener) {

        mUsbHostManager = usbHostManager;
        mUsbDevice = usbDevice;
        mListener = listener;
    }

    public void setPendingPermission(boolean pendingPermission) {
        mIsPendingPermission = pendingPermission;
    }

    public boolean isPendingPermission() {
        return mIsPendingPermission;
    }

    @Override
    public void open() {
        if (mIsConnected) {
            mListener.onOpen();
            return;
        }

        mDeviceConnection = mUsbHostManager.provideManager().openDevice(mUsbDevice);

        if (mDeviceConnection == null) {
            mListener.onError(HostError.OpenFail);
            return;
        }

        UsbDeviceConfiguration configuration
                = mUsbHostManager.provideConfigurationForDevice(mUsbDevice);

        if (configuration == null || configuration.deviceInterface == null
                || configuration.readEndPoint == null) {

            mListener.onError(HostError.NoConfigurationProvided);
            return;
        }

        mUsbInterface = configuration.deviceInterface;
        mReadEndPoint = configuration.readEndPoint;
        mWriteEndPoint = configuration.writeEndPoint;


        if(!(mDeviceConnection.claimInterface(mUsbInterface, true))) {
            mListener.onError(HostError.ClaimInterfaceFail);
            return;
        }

        mReceiverThread = new ReceiverThread("UsbHostSocket.ReaderThread");
        mReceiverThread.start();

        mRequestRouterThread = new RequestRouterThread("UsbHostSocket.RequestRouterThread");
        mRequestRouterThread.start();

        mIsConnected = true;


        if (mWriteEndPoint != null) {
            mSenderThread = new SenderThread("UsbHostSocket.SenderThread");
            mSenderThread.start();
        }

        mListener.onOpen();

    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public void write(byte[] data) {
        if (mIsConnected) {
            mSenderThread.send(data);
        }
    }

    @Override
    public void close() {
        if (mIsConnected) {
            mIsConnected = false;
            mIsDisconnecting = true;
            mReceiverThread.close();
            mSenderThread.send(mListener.onProvideCloseCommand());
        }
    }

    private void closeDevice() {
        try {
            mDeviceConnection.releaseInterface(mUsbInterface);
            mDeviceConnection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDataReceived(byte[] inboundData) {
        mListener.onRead(inboundData);
    }

    /**
     * It maybe called multiple times since closing a stream in one direction may generate an error
     * in the other direction. We pay attention only the first call.
     */
    private void handleError() {
        if (!isError) {
            isError = true;
            closeDevice();
            mUsbHostManager.disposeUsbHostSocket(this);
        }
    }

    class ReceiverThread extends HandlerThread {

        private static final int MAX_BUF_SIZE = 1024;
        private Handler mHandler;
        private boolean mThreadRunning;


        public ReceiverThread(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            mHandler = new Handler(getLooper());
            mHandler.post(mRunnable);
        }

        public void close() {
            mThreadRunning = false;
        }

        private Runnable mRunnable = new Runnable() {
            @Override
            public void run() {

                mThreadRunning = true;

                // Receiving loop
                while (mThreadRunning) {
                    try {

                        synchronized (mReadRequestMonitor) {
                            mInputByteBuffer = ByteBuffer.allocate(MAX_BUF_SIZE);
                            mReadRequest.initialize(mDeviceConnection, mReadEndPoint);
                            mReadRequest.queue(mInputByteBuffer, mInputByteBuffer.capacity());

                            mIsReadInQueue = true;

                            while (mIsReadInQueue) {
                                // Semaphore for reading, will be signaled from the Router Thread
                                wait();
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mThreadRunning = false;
                        handleError();
                    }
                }

                // Before leaving the Thread close the inputStream.
                try {

                    mHandler.removeCallbacksAndMessages(null);
                    mHandler.getLooper().quit();

                    if (mIsDisconnecting) {
                        mRequestRouterThread.close();
                        mSenderThread.close();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };

    }

    class SenderThread extends HandlerThread {

        private static final int MAX_BUF_SIZE = 1024;
        private Handler mHandler;
        private boolean mThreadStarted;

        public SenderThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            mHandler = new Handler(getLooper());
            mThreadStarted = true;
        }

        public void send(final byte[] outboundData) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {

                        if (mThreadStarted) {
                            UsbRequest usbRequest = new UsbRequest();
                            usbRequest.initialize(mDeviceConnection, mWriteEndPoint);
                            ByteBuffer byteBuffer = ByteBuffer.wrap(outboundData);
                            usbRequest.queue(byteBuffer, byteBuffer.position());
                            registerRequest(usbRequest);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        handleError();
                    }
                }
            });
        }

        public void close() {
            mThreadStarted = false;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.getLooper().quit();

            if (mIsDisconnecting) {
                closeDevice();
            }
        }

    }

    class RequestRouterThread extends HandlerThread {

        private Handler mHandler;
        private boolean mThreadRunning;

        public RequestRouterThread(String name) {
            super(name);
        }

        @Override
        public synchronized void start() {
            super.start();
            mHandler = new Handler(getLooper());
            mThreadRunning = true;

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    while (mThreadRunning) {
                        try {

                            UsbRequest comingRequest = mDeviceConnection.requestWait();

                            if (comingRequest != null && isWriteRequest(comingRequest)) {
                                // Ignore Write requests, only attend reading ones
                                comingRequest.close();
                                mWriteRequestQueue.remove(comingRequest);

                            } else if (comingRequest != null && comingRequest.equals(mReadRequest)){

                                mListener.onRead(mInputByteBuffer.array());

                                // Let the reader thread knows it can post another usb request
                                mIsReadInQueue = false;
                                synchronized (mReadRequestMonitor) {
                                    mReadRequestMonitor.notifyAll();
                                }

                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            handleError();
                        }
                    }
                }
            });

        }

        public void close() {
            mThreadRunning = false;
            mHandler.removeCallbacksAndMessages(null);
            mHandler.getLooper().quit();
        }

    }

    private void registerRequest(UsbRequest usbRequest) {
        mWriteRequestQueue.offer(usbRequest);
    }

    private boolean isWriteRequest(UsbRequest comingRequest) {
        for (UsbRequest usbRequest : mWriteRequestQueue) {
            if (usbRequest.equals(comingRequest)) {
                return true;
            }
        }
        return false;
    }

    // region: package private

    /* package */ void handleUsbDeviceDetach() {
        handleError();
    }

    /* package */ UsbDevice getUsbDevice() {
        return mUsbDevice;
    }

    // endregion

}
