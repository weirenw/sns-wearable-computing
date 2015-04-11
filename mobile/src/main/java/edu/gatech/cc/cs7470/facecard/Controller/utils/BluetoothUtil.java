package edu.gatech.cc.cs7470.facecard.Controller.utils;

import android.bluetooth.BluetoothAdapter;

/**
 * Created by miseonpark on 3/10/15.
 */
public class BluetoothUtil {

    private static final String TAG = "FaceCard BluetoothUtil";

    /**
     * getBluetoothId
     * @return Bluetooth UUID for the device
     */
    public String getBluetoothId(){

        try {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
	        return adapter.getAddress();
            /*Method getUuidsMethod = BluetoothAdapter.class.getDeclaredMethod("getUuids", null);
            ParcelUuid[] uuids = (ParcelUuid[]) getUuidsMethod.invoke(adapter, null);
            for (ParcelUuid uuid : uuids) {
                Log.d(TAG, "UUID: " + uuid.getUuid().toString());
            }
            return uuids[0].getUuid().toString();*/
        }catch(Exception e){
            return "";
        }

    }
}
