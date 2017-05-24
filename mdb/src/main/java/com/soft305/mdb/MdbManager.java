package com.soft305.mdb;

import com.soft305.mdb.device.BillValidator;
import com.soft305.mdb.device.Cashless1;
import com.soft305.mdb.device.Cashless2;
import com.soft305.mdb.device.CoinAcceptor;
import com.soft305.mdb.device.Display;
import com.soft305.mdb.device.Peripheral;
import com.soft305.mdb.device.PeripheralHelper;
import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.log.LoggerListener;
import com.soft305.mdb.parser.ChunkQueue;
import com.soft305.mdb.util.ByteUtil;
import com.soft305.mdb.util.StringUtil;
import com.soft305.socket.Socket;
import java.util.EnumMap;
import java.util.Map;


public class MdbManager {


    private static final String TAG = "MdbManager";

    private Socket mSocket;
    private boolean isSocketOpen;
    private VmcInput mVmcInput;
    private MdbPeripheral mCurPeripheral;
    private Map<Peripheral, MdbPeripheral> mEnumMdbPeripheralMap;
    //private Reducer mCurReducer;

    private LoggerListener mLoggerListener;


    public MdbManager(Socket socket) {
        mSocket = socket;

        mEnumMdbPeripheralMap = new EnumMap<>(Peripheral.class);
        mEnumMdbPeripheralMap.put(Peripheral.CoinAcceptor, new CoinAcceptor(this));
        mEnumMdbPeripheralMap.put(Peripheral.Cashless1, new Cashless1(this));
        mEnumMdbPeripheralMap.put(Peripheral.Display, new Display(this));
        mEnumMdbPeripheralMap.put(Peripheral.BillValidator, new BillValidator(this));
        mEnumMdbPeripheralMap.put(Peripheral.Cashless2, new Cashless2(this));
    }

    public void start() {
        mSocket.openUsbAoa(mUsbAoaListener);
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
            mCurPeripheral = new Cashless2(MdbManager.this);
        }

        @Override
        public void onRead(final byte[] data) {
            if (isSocketOpen) {
                mLoggerListener.onInputVmcData(StringUtil.fromByteArrayToHexString(data));

                // Feed the Vmc Input queue before being parsed
                mVmcInput.chunkQueue.offer(data);
                route();
            }
        }
    };


    private enum State {Idle, Routing}
    private State mState = State.Idle;
    private byte[] head;

    // The longest data that can be sent is 36. So no command will ever wait for more data. So we
    // will use it as a guard for if a PeripheralController do not consume the bytes from the Queue
    private int CyclesRoutingMax = 36;
    private int mCyclesRoutingCount = 0;

    private PeripheralHelper mPeripheralHelper = new PeripheralHelper();

    private void route() {

        // Parse the whole Input Queue until empty.
        while (mVmcInput.chunkQueue.peek(2) != null) {

            if (State.Idle.equals(mState)) {

                head = mVmcInput.chunkQueue.peek(2);
                Peripheral peripheral = mPeripheralHelper.getPeripheralByCmdAddress(head[1]);

                if (ByteUtil.areEqual(head[0], (byte)0x01) && peripheral != null) {
                    setRoutingState();
                    mCurPeripheral = mEnumMdbPeripheralMap.get(peripheral);

                } else {
                    // If the received head is garbage then discard it by consuming it.
                    mVmcInput.chunkQueue.consume(2);
                }

            } else if (State.Routing.equals(mState)) {

                int lenSnapshotBefore = mVmcInput.chunkQueue.getAvailableLength();
                mVmcInput.chunkQueue.setSnapShot(true);

                if (mCurPeripheral.processCommand(mVmcInput)) {
                    // Peripheral shall return true when processed the incoming vmc command.
                    // It shall removed the bytes from the Queue by calling consume(n) otherwise
                    // we will detect the bad implementation with the code bellow.
                    int lenSnapshotAfter = mVmcInput.chunkQueue.getAvailableLength();
                    int afterSnapshotOffer = mVmcInput.chunkQueue.getAfterSnapShotOffer();
                    mVmcInput.chunkQueue.setSnapShot(false);

                    // lenSnapshotAfter = lenSnapshotBefore - consumed + afterSnapshotOffer
                    // or
                    // lenSnapshotBefore - consumed = lenSnapshotAfter - afterSnapshotOffer
                    if (lenSnapshotBefore >= lenSnapshotAfter - afterSnapshotOffer) {
                        mVmcInput.chunkQueue.consume(2);
                    }

                    setIdleState();
                }

                // Some wrong peripheral implementations may forget to consume its bytes, so we
                // guard against infinite looping by allowing a maximum of Routing cycles.
                mCyclesRoutingCount ++;
                if (mCyclesRoutingCount >= CyclesRoutingMax) {
                    // Remove this Peripheral Head from the Queue and keep processing as in Idle.
                    setIdleState();
                    mVmcInput.chunkQueue.consume(2);
                }
            }
        }

    }

    private void setIdleState () {
        mState = State.Idle;
    }

    private void setRoutingState () {
        mState = State.Routing;
        mCyclesRoutingCount = 0;
    }


    //******************************************** LOGGER ******************************************

    // This method must be removed in production
    public void setLoggerListener(LoggerListener loggerListener) {
        mLoggerListener = loggerListener;
    }


    //******************************************** TEST ********************************************

    // Method Only for testing should be deleted in production using proguard
    public void forTestInjectVmcInput(VmcInput testVmcInput) {
        mVmcInput = testVmcInput;
    }

    public void forTestDoRoute() {
        route();
    }

}
