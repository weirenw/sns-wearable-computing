package edu.gatech.cc.cs7470.facecard.View;

/**
 * Created by weirenwang on 3/22/15.
 */


import android.os.AsyncTask;

import com.amazonaws.services.simpledb.model.SelectRequest;

import java.util.List;

public class ReadFromDB extends AsyncTask<Void, Void, Info[]> {



    public static Info [] getAllUsers() throws Exception
    {
        SelectRequest selectRequest=  new SelectRequest("select * from user_info").withConsistentRead(true);

        List<com.amazonaws.services.simpledb.model.Item> items  = ConnectionAWS.getAwsSimpleDB().select(selectRequest).getItems();

        try
        {
            com.amazonaws.services.simpledb.model.Item temp1;
            int size= items.size();
            Info [] userList= new  Info[size];

            for(int i=0; i<size;i++)
            {
                temp1= ((com.amazonaws.services.simpledb.model.Item)items.get( i ));

                List<com.amazonaws.services.simpledb.model.Attribute> tempAttribute= temp1.getAttributes();
                userList[i]= new Info();
                for(int j=0; j< tempAttribute.size();j++)
                {
                    if(tempAttribute.get(j).getName().equals("BTID"))
                    {
                        userList[i].BTID=tempAttribute.get(j).getValue();
                    }
                    else if(tempAttribute.get(j).getName().equals("userName"))
                    {
                        userList[i].userName =tempAttribute.get(j).getValue();
                    }
                }
            }
            return userList;
        }
        catch( Exception eex)
        {
            throw new Exception("FIRST EXCEPTION", eex);
        }
    }

    @Override
    protected Info[] doInBackground(Void... params) {
        // TODO Auto-generated method stub
        try {
            return getAllUsers();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

}

