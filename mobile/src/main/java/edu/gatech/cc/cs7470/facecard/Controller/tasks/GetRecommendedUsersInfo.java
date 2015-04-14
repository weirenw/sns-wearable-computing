package edu.gatech.cc.cs7470.facecard.Controller.tasks;

import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.View.activities.MainActivity;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.FaceCard;

/**
 * Created by miseonpark on 3/9/15.
 */
public class GetRecommendedUsersInfo extends AsyncTask<ArrayList<String>, String, String> {

    private static final String TAG = "FaceCard GetRecommendedUsersInfo";
    private ArrayList<FaceCard> faceCards = new ArrayList<FaceCard>();
    private MainActivity mActivity;
    public GetRecommendedUsersInfo(MainActivity callingActivity)
    {
        mActivity=callingActivity;
    }

    /* Register Bluetooth */
    @Override
    protected String doInBackground(ArrayList<String>... params) {

        ArrayList<String> bluetoothMACs = params[0];

        //create string
        String rest = "recommender.php?source=" + bluetoothMACs.get(0) + "&target=";
        for(int i = 1; i < bluetoothMACs.size(); i++){
            rest+=(bluetoothMACs.get(i));
            if(i!=(bluetoothMACs.size()-1))
                rest+=",";
        }


        rest = rest.replace(" ", "%20");
        Log.d(TAG, rest);

        InputStream is = null;
        try {
            URL url = new URL(Constants.REGISTER_ACCOUNT_URL + rest);
            Log.d(TAG,"executing GET request, URL = " + url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            is = conn.getInputStream();
            parseUsers(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }


    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        mActivity.recommenderCallBack(faceCards);

    }



    private void parseUsers(InputStream stream) throws IOException{
        JsonReader reader = new JsonReader(new InputStreamReader(stream, "UTF-8"));
        reader.beginObject(); // {
        reader.nextName();  // target_list
        reader.beginArray();  // [
        while (reader.hasNext())
            faceCards.add(parseUser(reader));
        reader.endArray();   // ]
        reader.endObject();  // }
        reader.close();
    }

    private FaceCard parseUser(JsonReader reader) throws IOException{
        FaceCard user;
        String bluetoothId="";
        String accountId=""; // this is the email address
        String major="";
        String name="";
        String tag="";
        String token;
        reader.beginObject();

        while (reader.hasNext()){
            token = reader.nextName();
            switch(token){
                case("bluetooth_id"):
                    bluetoothId = reader.nextString();
                    break;
                case("google_account"):
                    accountId = reader.nextString();
                    break;
                case("major"):
                    major = reader.nextString();
                    break;
                case("name"):
                    name = reader.nextString();
                    break;
                case("personal_tags"):
                    tag = reader.nextString();
                    break;
                default:
            }
        }
        reader.endObject();
        user = new FaceCard(bluetoothId, accountId, major, name, tag);

        return user;
    }

}
