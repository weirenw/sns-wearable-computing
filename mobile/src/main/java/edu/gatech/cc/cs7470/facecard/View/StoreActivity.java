package edu.gatech.cc.cs7470.facecard.View;

/**
 * Created by weirenwang on 3/22/15.
 */

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import edu.gatech.cc.cs7470.facecard.R;

public class StoreActivity extends Activity {
    TextView nameText;
    RatingBar ratingValue;
    TextView descriptionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store);
        Button addButton;
        addButton= (Button) findViewById(R.id.addButton);
        nameText=(TextView)findViewById(R.id.name);
        ratingValue=(RatingBar)findViewById(R.id.ratingBar1);
        descriptionText=(TextView)findViewById(R.id.Description);;//Description
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                try
                {// TODO Auto-generated method stub
                    Info object = new Info();
                    // nameText.getText need to be revised.
                    object.BTID=nameText.getText().toString();
                    object.userName=descriptionText.getText().toString();

                    //DbOperation.saveUserInfo(object);

                    new StoreToDB().execute(object).get();
                    Toast.makeText(getApplicationContext(), "New User Info added", Toast.LENGTH_LONG).show();
                }
                catch(Exception e)
                {

                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.store, menu);
        return true;
    }

}
