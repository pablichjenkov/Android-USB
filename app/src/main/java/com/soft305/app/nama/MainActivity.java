package com.soft305.app.nama;

import android.hardware.usb.UsbAccessory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.soft305.mdb.log.LoggerListener;
import com.soft305.mdb.MdbManager;
import com.soft305.socket.Socket;
import com.soft305.socket.usb.UsbAoaManager;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private UsbAoaManager mUsbAoaManager;
    private Socket mSocket;

    private MdbManager mMdbManager;
    private TextView mTxtMdbBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsbAoaManager = new UsbAoaManager(this);
        mUsbAoaManager.probe(new UsbAoaManager.Listener() {
            @Override
            public void onSelectUsbAoa(UsbAccessory[] accessoryArray) {
                mUsbAoaManager.selectAoa(accessoryArray[0]);
            }

            @Override
            public void onSocketCreated(Socket socket) {
                mSocket = socket;

                //Here is the connection with the MDB Library
                mMdbManager = new MdbManager(mSocket);
                mMdbManager.setLoggerListener(mLoggerListener);
                mMdbManager.start();

            }
        });



        findViewById(R.id.btn_begin_session).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: send purchase input to State Machine. It will send
            }
        });

        findViewById(R.id.btn_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTxtMdbBus.setText("");
            }
        });

        mTxtMdbBus = (TextView) findViewById(R.id.txt_mdb_bus);
        mTxtMdbBus.setMovementMethod(new ScrollingMovementMethod());

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUsbAoaManager != null) {
            mUsbAoaManager.close();
        }
    }


    private LoggerListener mLoggerListener = new LoggerListener() {

        @Override
        public void onError(final String errorInfo) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append("\n**************************************************\n");
                    mTxtMdbBus.append(errorInfo + "\n");
                }
            });
        }

        @Override
        public void onInputVmcData(final String dataHexFormat) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append(" << : " + dataHexFormat + "\n");
                }
            });

        }

        @Override
        public void onOutputVmcData(final String dataHexFormat) {
            // This method is called from a handler thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTxtMdbBus.append(" >> : " + dataHexFormat + "\n");
                }
            });
        }
    };

}
