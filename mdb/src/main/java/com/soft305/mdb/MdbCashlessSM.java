package com.soft305.mdb;

import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.parse.ChunkQueue;
import com.soft305.mdb.reducer.InactiveReducer;
import com.soft305.mdb.util.StringUtil;
import com.soft305.socket.Socket;


public class MdbCashlessSM {

    private static final String TAG = "MdbCashless";

    private Socket mSocket;
    private boolean isSocketOpen;
    private VmcInput mVmcInput;
    private Reducer mCurReducer;

    private LoggerListener mLoggerListener;


    public MdbCashlessSM(Socket socket) {
        mSocket = socket;
    }

    public void start() {
        mSocket.openUsbAoa(mUsbAoaListener);
    }

    public void setReducer(Reducer newReducer) {
        mCurReducer = newReducer;
    }

    /**
     *   Internal method to be used by Command Handlers. Possible to be hidden later
     * */
    public void send(byte[] data) {
        if (isSocketOpen) {
            mLoggerListener.onOutputVmcData(StringUtil.fromByteArrayToHexString(data));
            mSocket.write(data);
        }
    }

    private Socket.UsbAoaListener mUsbAoaListener = new Socket.UsbAoaListener() {
        @Override
        public void onError(Socket.UsbAoaErrorInfo errorInfo) {
            mLoggerListener.onError(errorInfo.info);
            isSocketOpen = false;
        }

        @Override
        public void onOpen() {
            isSocketOpen = true;
            mVmcInput = new VmcInput(new ChunkQueue());
            mCurReducer = new InactiveReducer(MdbCashlessSM.this);
        }

        @Override
        public void onRead(final byte[] data) {
            if (isSocketOpen) {
                mLoggerListener.onInputVmcData(StringUtil.fromByteArrayToHexString(data));

                // Feed Vmc Input
                mVmcInput.chunkQueue.offer(data);

                mCurReducer.inputVMC(mVmcInput);
            }
        }
    };


    //********************************** LOGGER **********************************
    // This method must be removed in production
    public void setLoggerListener(LoggerListener loggerListener) {
        mLoggerListener = loggerListener;
    }
}
