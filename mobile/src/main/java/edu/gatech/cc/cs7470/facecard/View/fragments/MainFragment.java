package edu.gatech.cc.cs7470.facecard.View.fragments;

/**
 * Created by miseonpark on 3/23/15.
 */

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.InputStream;

import edu.gatech.cc.cs7470.facecard.Constants;
import edu.gatech.cc.cs7470.facecard.View.activities.Model.Profile;
import edu.gatech.cc.cs7470.facecard.R;
import edu.gatech.cc.cs7470.facecard.View.activities.MainActivity;
import edu.gatech.cc.cs7470.facecard.View.uihelpers.RoundImageHelper;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment {

    private static String TAG = "PlaceholderFragment";

    private static MainActivity activity;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";
    private TextView tv_profile_name;
    private TextView tv_profile_tagline;
    private TextView tv_profile_organization;
    private TextView tv_phone;
    private TextView tv_email;
    private TextView tv_website;
    private ImageView iv_profile_picture;
    private LinearLayout ll_profile_background;


    //links
    private TextView tv_google_link;

    private GoogleApiClient mGoogleApiClient;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MainFragment newInstance() {
        MainFragment mFragment = new MainFragment();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return mFragment;
    }

    public MainFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        activity = (MainActivity) this.getActivity();
        mGoogleApiClient = activity.getGoogleApiClient();

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        tv_profile_name = (TextView)rootView.findViewById(R.id.profile_name);
        tv_profile_tagline = (TextView)rootView.findViewById(R.id.profile_tagline);
        tv_profile_organization = (TextView)rootView.findViewById(R.id.profile_organization);

//            tv_phone = (TextView)rootView.findViewById(R.id.profile_phone);
        tv_email = (TextView)rootView.findViewById(R.id.profile_email);
        tv_website = (TextView)rootView.findViewById(R.id.profile_website);

        iv_profile_picture = (ImageView)rootView.findViewById(R.id.profile_picture);
        ll_profile_background = (LinearLayout)rootView.findViewById(R.id.profile_cover);

        //links
        tv_google_link = (TextView)rootView.findViewById(R.id.google_link);

        populateProfileInfo();

        return rootView;
    }



    private void populateProfileInfo(){
        try {
            if (mGoogleApiClient.isConnected() && Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Profile profile = activity.getProfile();

                String profile_picture_url = profile.getProfile_picture_url();
                String profile_cover_url = profile.getProfile_cover_url();

                tv_profile_name.setText(profile.getName());
                tv_profile_tagline.setText(profile.getTagline());
                tv_profile_organization.setText(profile.getOrganization());

                //Contacts
//                    tv_phone.setText(profile.getPhone());
                tv_email.setText(profile.getEmail());
//                    tv_website.setText(profile.getWebsite());

                //Links
                tv_google_link.setText(profile.getGoogle_link());

                // by default the profile url gives 50x50 px image only
                // we can replace the value with whatever dimension we want by
                // replacing sz=X
                if(profile_picture_url.length()>0) {
                    profile_picture_url = profile_picture_url.substring(0,
                            profile_picture_url.length() - 2)
                            + Constants.PROFILE_PIC_SIZE;
                    new LoadProfileImage(iv_profile_picture).execute(profile_picture_url);
                }
                if(profile_cover_url.length()>0) {
                    new LoadProfileImage(ll_profile_background).execute(profile_cover_url);
                }

            } else {
//                    Toast.makeText(getApplicationContext(),
//                            "Person information is null", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Background Async task to load user profile picture from url
     * */
    private class LoadProfileImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        LinearLayout coverImage;
        RoundImageHelper roundImageHelper;
        boolean isCoverImage;

        public LoadProfileImage(ImageView bmImage) {
            this.bmImage = bmImage;
            this.isCoverImage = false;
            roundImageHelper = new RoundImageHelper();
        }

        public LoadProfileImage(LinearLayout coverImage) {
            this.coverImage = coverImage;
            this.isCoverImage = true;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                if (isCoverImage) {
                    Drawable background = new BitmapDrawable(result);
                    coverImage.setBackground(background);
                } else {
                    bmImage.setImageBitmap(roundImageHelper.getRoundedCornerBitmap(result, Constants.PROFILE_PIC_RADIUS));
                }
            }
        }
    }
}