package edu.gatech.cc.cs7470.facecard;

/**
 * Created by miseonpark on 2/24/15.
 */
public class Constants {

    public static final String PACKAGE_NAME = "edu.gatech.cc.cs7470.facecard";

    public static final int REQUEST_ENABLE_BT = 1; //callback code for bluetooth enabling dialog

    //profile layout settings
    public static final int PROFILE_PIC_SIZE = 100;
    public static final int PROFILE_PIC_RADIUS = 50;

    //shared preferences labels
    public static final String SHARED_PREFERENCES_ACCOUNT = "account"; //Google+ account
    public static final String SHARED_PREFERENCES_BLUETOOTH = "bluetooth"; //Bluetooth

    //TODO: change url
    public static final String REGISTER_ACCOUNT_URL = "http://ec2-54-68-110-119.us-west-2.compute.amazonaws.com/facecard/setAccount.php?";
}
