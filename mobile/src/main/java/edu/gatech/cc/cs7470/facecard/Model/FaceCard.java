package edu.gatech.cc.cs7470.facecard.Model;

import java.io.Serializable;

/**
 * Created by miseonpark on 3/24/15.
 */
public class FaceCard implements Serializable {
	private static final long serialVersionUID = 103701L;

	private String bluetoothId, accountId, name, tag, major;

	public FaceCard(String bluetoothId, String accountId, String name, String tag, String major){
		this.bluetoothId = bluetoothId;
		this.accountId = accountId;
		this.name = name;
		this.tag = tag;
		this.major = major;
	}

	public String getMajor() {
		return major;
	}

	public void setMajor(String major) {
		this.major = major;
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
}
