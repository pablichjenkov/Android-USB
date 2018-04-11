# Android-USB
The existing samples that ilustrate how to use the Android USB API are a little messy. This apply for both modes of USB Host 
and Accessory. This library is made with the purpose of transform the existing API into a more Socket oriented API. The 
purpose is to remove the boilerplate code of scanning for attached devices, select a specific device and then create a 
connection.

```groovy
dependencies {
    implementation "com.letmeaccess:usb:1.0.0"
}
```

#### Using the library


```java
private UsbAoaManager mUsbAoaManager = new UsbAoaManager(Context);
private Socket mSocket;

mUsbAoaManager.probe(new UsbAoaManager.Listener() {
            @Override
            public void onSelectAccessory(UsbAccessory[] accessoryArray) {
                mUsbAoaManager.createSocket(accessoryArray[0], mAccessoryListener);
            }

            @Override
            public void onSocketCreated(Socket socket) {
                mSocket = socket;
                mSocket.open();
            }
        });

private Socket.AccessoryListener mAccessoryListener = new Socket.AccessoryListener() {
        @Override
        public void onError(Socket.AccessoryError error) {
            isUsbSocketOpen = false;
        }

        @Override
        public void onOpen() {
            isUsbSocketOpen = true;
            mSocket.write(new byte[]{(byte)'h',(byte)'e',(byte)'l',(byte)'l',(byte)'o'});
        }

        @Override
        public void onRead(byte[] data) {
            Timber.d("AccessoryListener.onRead() -> " + new String(data));
        }
    };

```

### Enjoy and feel free to PR.

