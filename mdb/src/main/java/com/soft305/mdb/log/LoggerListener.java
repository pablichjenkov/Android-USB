package com.soft305.mdb.log;

/**
 * Created by pablo on 5/21/17.
 */
public interface LoggerListener {
    void onError(String errorInfo);
    void onInputVmcData (String dataHexFormat);
    void onOutputVmcData (String dataHexFormat);

}
