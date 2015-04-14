package edu.gatech.cc.cs7470.facecard.Controller.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.FaceCard;

/**
 * Created by miseonpark on 3/24/15.
 */
public class DiscoverNearbyPeopleTask extends AsyncTask<ArrayList<String>, String, String> {

    private static final String TAG = "FaceCard DiscoverNearbyPeopleTask";
    private FaceCard faceCard;

    /* Get Info for Facecard */
    @Override
    protected String doInBackground(ArrayList<String> ... params) {

        String srcBTID = params[0].get(0);
        int lastIndex = params[0].size()-1;
        ArrayList<String> targetBTIDs = (ArrayList<String>) params[0].subList(1,lastIndex);

        //create string
        String rest = "?source=" + srcBTID + "&target=";
        for(String i : targetBTIDs)
            rest = rest + ","+ i;

        rest = rest.replace(" ", "%20");
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

            String bluetoothId="";
            String accountId="";
            String name="";
            String tag="";

            int counter = 0;
            while((bufferedStrChunk = bufferedReader.readLine()) != null){
                switch(counter){
                    case 1:
                        bluetoothId = bufferedStrChunk;
                        break;
                    case 2:
                        accountId = bufferedStrChunk;
                        break;
                    case 3:
                        name = bufferedStrChunk;
                        break;
                    case 4:
                        tag = bufferedStrChunk;
                //        faceCard = new FaceCard(bluetoothId, accountId, name, tag);
                        break;
                    default:
                        break;
                }
                counter++;
            }

            Log.d(TAG, stringBuilder.toString());
            return "successful";

        } catch (ClientProtocolException e) {
            Log.d(TAG, e.toString());
        } catch (IOException e) {
            Log.d(TAG, e.toString());
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

        return null;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        if(result.equals("successful")){
            Log.d(TAG, "successful");
        }else{
            Log.d(TAG, "failed");
        }
    }
}
