package edu.gatech.cc.cs7470.facecard.View;

/**
 * Created by weirenwang on 3/22/15.
 */


import android.os.AsyncTask;

import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;

import java.util.ArrayList;
import java.util.List;

public class StoreToDB extends AsyncTask<Info, Void, Void>  {

    // 2. Create Domain and save movie information in domain
    public static void saveUserInfo(String BTID, String userName)
    {
        try {

            ConnectionAWS.getAwsSimpleDB().createDomain(new CreateDomainRequest( "user_info"));
            List<ReplaceableAttribute> attribute= new ArrayList<ReplaceableAttribute>(1);
            attribute.add(new ReplaceableAttribute().withName("BTID").withValue(BTID));
            attribute.add(new ReplaceableAttribute().withName("userName").withValue(userName));
            ConnectionAWS.awsSimpleDB.putAttributes(new PutAttributesRequest("user_info",BTID, attribute));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected Void doInBackground(Info... params) {
        // TODO Auto-generated method stub
        saveUserInfo(params[0].BTID, params[0].userName);
        return null;
    }

}