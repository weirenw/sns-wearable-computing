package edu.gatech.cc.cs7470.facecard.Model;

/**
 * Created by miseonpark on 3/9/15.
 */
public class Bluetooth {

    private String bluetoothId;
    private String accountId;

    public Bluetooth(String bluetoothId, String accountId){
        this.bluetoothId = bluetoothId;
        this.accountId = accountId;
    }

    public String getBluetoothId() {
        return bluetoothId;
    }

    public void setBluetoothId(String bluetoothId) {
        this.bluetoothId = bluetoothId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }


}
