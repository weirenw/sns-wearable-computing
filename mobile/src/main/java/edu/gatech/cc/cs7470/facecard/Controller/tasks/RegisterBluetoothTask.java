package edu.gatech.cc.cs7470.facecard.Controller.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import edu.gatech.cc.cs7470.facecard.Constants;

/**
 * Created by miseonpark on 3/9/15.
 */
public class RegisterBluetoothTask extends AsyncTask<String, String, String> {

    private static final String TAG = "RegisterBluetoothTask";

    /* Register Bluetooth */
    @Override
    protected String doInBackground(String... params) {

        String accountId = params[0];
        String bluetoothId = params[1];
        String name = params[2];
        String tag = params[3];

        //create string
        String rest = "bluetooth_id=" + bluetoothId + "&google_account=" + accountId
                + "&google_password=" + "pwd" + "&name=" +  name + "&personal_tags=" + tag;

        try {rest = URLEncoder.encode(rest, "UTF-8"); } catch (Exception e) {}
        Log.d(TAG, rest);

        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(Constants.REGISTER_ACCOUNT_URL + rest);

        try { //get response
            HttpResponse httpResponse = httpClient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String bufferedStrChunk = null;

            while((bufferedStrChunk = bufferedReader.readLine()) != null){
                stringBuilder.append(bufferedStrChunk);
            }

            Log.d(TAG, stringBuilder.toString());

            return stringBuilder.toString();

        } catch (Exception e) {
            Log.d(TAG, e.toString());
	        return "false";
        }

//        BasicNameValuePair accountPair = new BasicNameValuePair("google_account", accountId);
//        BasicNameValuePair passwordPair = new BasicNameValuePair("google_password", accountId);
//        BasicNameValuePair bluetoothPair = new BasicNameValuePair("bluetooth_id", bluetoothId);
//        BasicNameValuePair firstNamePair = new BasicNameValuePair("first_name", firstName);
//        BasicNameValuePair lastNamePair = new BasicNameValuePair("last_name", lastName);
//        BasicNameValuePair tagPair = new BasicNameValuePair("personal_tags", tag);
//        List<NameValuePair> nameValuePairList = new ArrayList<>();
//        nameValuePairList.add(bluetoothPair);
//        nameValuePairList.add(accountPair);
//        nameValuePairList.add(passwordPair);
//        nameValuePairList.add(firstNamePair);
//        nameValuePairList.add(lastNamePair);
//        nameValuePairList.add(tagPair);
//        try {
//            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
//            httpPost.setEntity(urlEncodedFormEntity);
//
//            try { //get response
//                HttpResponse httpResponse = httpClient.execute(httpPost);
//
//                InputStream inputStream = httpResponse.getEntity().getContent();
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder stringBuilder = new StringBuilder();
//                String bufferedStrChunk = null;
//
//                while((bufferedStrChunk = bufferedReader.readLine()) != null){
//                    stringBuilder.append(bufferedStrChunk);
//                }
//
//                Log.d(TAG, stringBuilder.toString());
//
//                return stringBuilder.toString();
//
//            } catch (ClientProtocolException e) {
//                Log.d(TAG, e.toString());
//            } catch (IOException e) {
//                Log.d(TAG, e.toString());
//            }
//
//        } catch (UnsupportedEncodingException e) {
//            Log.d(TAG, e.toString());
//        }


    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("true")){
            Log.d(TAG, "successfully registered bluetooth id");
        }else{
            Log.d(TAG, "bluetooth id registration failed");
        }
    }

}
