package edu.gatech.cc.cs7470.facecard.View.activities.Model;

/**
 * Created by miseonpark on 3/24/15.
 */
public class FaceCard {

    private String bluetoothId, email, major, name, tag;

    public FaceCard(String bluetoothId, String email, String major, String name, String tag){
        this.bluetoothId = bluetoothId;
        this.email = email;
        this.name = name;
        this.tag = tag;
        this.major=major;
    }

    public String getBluetoothId() {
        return bluetoothId;
    }

    public void setBluetoothId(String bluetoothId) {
        this.bluetoothId = bluetoothId;
    }

    public String getAccountId() {
        return email;
    }

    public void setAccountId(String accountId) {
        this.email = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }
}
