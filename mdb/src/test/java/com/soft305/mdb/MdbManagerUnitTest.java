package com.soft305.mdb;

import com.soft305.mdb.input.VmcInput;
import com.soft305.mdb.parser.ChunkQueue;
import com.soft305.socket.Socket;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.Silent.class)
public class MdbManagerUnitTest {

    private byte[] inputGarbage = new byte[]{
              0x51,(byte)0xf8 // garbage
            , 0x01,0x08 // CoinAcceptor
            , 0x51,(byte)0xf8 // garbage
            , 0x00,0x10 // Cashless1
            , 0x52,(byte)0xf9 // garbage
            , 0x01,0x18 // CommunicationsGateway
            , 0x53,(byte)0xfa // garbage
            , 0x01,0x20 // Display
            , 0x54,(byte)0xfb // garbage
            , 0x01,0x28 // EnergySystem
            , 0x55,(byte)0xfc // garbage
            , 0x01,0x30 // BillValidator
            , 0x56,(byte)0xfd // garbage
            , 0x01,0x40 // UniversalSatellite1
            , 0x57,(byte)0xfe // garbage
            , 0x01,0x48 // UniversalSatellite2
            , 0x58,(byte)0xff // garbage
            , 0x01,0x50 // UniversalSatellite3
            , 0x61,(byte)0xa8 // garbage
            , 0x01,0x58 // CoinHopper1
            , 0x62,(byte)0xa9 // garbage
            , 0x01,0x60 // Cashless2
            , 0x63,(byte)0xaa // garbage
            , 0x01,0x68 // AgeVerification
            , 0x64,(byte)0xab // garbage
            , 0x01,0x70 // CoinHopper2
            , 0x65,(byte)0xac // garbage
    };

    private byte[] inputResetRound = new byte[]{
              0x01,0x08 // CoinAcceptor
            , 0x01,0x10 // Cashless1
            , 0x01,0x18 // CommunicationsGateway
            , 0x01,0x20 // Display
            , 0x01,0x28 // EnergySystem
            , 0x01,0x30 // BillValidator
            , 0x01,0x40 // UniversalSatellite1
            , 0x01,0x48 // UniversalSatellite2
            , 0x01,0x50 // UniversalSatellite3
            , 0x01,0x58 // CoinHopper1
            , 0x01,0x60 // Cashless2
            , 0x01,0x68 // AgeVerification
            , 0x01,0x70 // CoinHopper2
    };


    @Test
    public void vmcSendGarbageTest() {


        Socket socketMock = Mockito.mock(Socket.class);
        Mockito.doNothing().when(socketMock).openUsbAoa(dummyAoaListener);

        MdbManager mdbManager = new MdbManager(socketMock);
        mdbManager.start();

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(inputGarbage);
        VmcInput vmcInput = new VmcInput(chunkQueue);

        mdbManager.forTestInjectVmcInput(vmcInput);
        mdbManager.forTestDoRoute();

        int availableLength = chunkQueue.getAvailableLength();

        // Test everything was consumed
        Assert.assertEquals(0, availableLength);

    }

    @Test
    public void vmcResetAllDevicesTest() {


        Socket socketMock = Mockito.mock(Socket.class);
        Mockito.doNothing().when(socketMock).openUsbAoa(dummyAoaListener);

        MdbManager mdbManager = new MdbManager(socketMock);
        mdbManager.start();

        ChunkQueue chunkQueue = new ChunkQueue();
        chunkQueue.offer(inputResetRound);
        VmcInput vmcInput = new VmcInput(chunkQueue);

        mdbManager.forTestInjectVmcInput(vmcInput);
        mdbManager.forTestDoRoute();

        int availableLength = chunkQueue.getAvailableLength();

        // Test everything was consumed
        Assert.assertEquals(0, availableLength);

    }


    Socket.UsbAoaListener dummyAoaListener = new Socket.UsbAoaListener() {
        @Override
        public void onError(Socket.UsbAoaErrorInfo errorInfo) {

        }

        @Override
        public void onOpen() {

        }

        @Override
        public void onRead(byte[] data) {

        }
    };

}
