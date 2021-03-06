package com.codepath.apps.twitterclient.activities;

import android.os.Parcel;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TabWidget;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.fragments.TweetDetailFragment;
import com.codepath.apps.twitterclient.fragments.TweetListFragment;
import com.codepath.apps.twitterclient.fragments.UserTimelineFragment;
import com.codepath.apps.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;
import org.parceler.Parcels;

import cz.msebera.android.httpclient.Header;

import static com.codepath.apps.twitterclient.R.string.tweet;
import static com.raizlabs.android.dbflow.config.FlowManager.getContext;

public class ProfileActivity extends AppCompatActivity implements TweetDetailFragment.TweetDetailsListener {

    TwitterClient client;
    User user;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        client = TwitterApplication.getRestClient();

        if (getIntent().hasExtra("user")) {
            user = Parcels.unwrap(getIntent().getParcelableExtra("user"));
            getSupportActionBar().setTitle(user.getScreenname());
            populateProfileHeader(user);
            if (savedInstanceState == null) {
                // Create the UserTimelineFragment
                UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(user.getScreenname());
                // Display the UserFragment within this activity dynamically
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.flContainer, fragmentUserTimeline);
                ft.commit();
            }

        } else {
            // Getting app user account info
            client.getUserInfo(new JsonHttpResponseHandler(){
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    user = User.fromJSON(response);
                    getSupportActionBar().setTitle(user.getScreenname());
                    populateProfileHeader(user);

                    if (savedInstanceState == null) {
                        // Create the UserTimelineFragment
                        UserTimelineFragment fragmentUserTimeline = UserTimelineFragment.newInstance(user.getScreenname());
                        // Display the UserFragment within this activity dynamically
                        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.flContainer, fragmentUserTimeline);
                        ft.commit();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    super.onFailure(statusCode, headers, responseString, throwable);
                }
            });


        }


    }

    private void populateProfileHeader(User user) {
        TextView tvName = (TextView) findViewById(R.id.tvName);
        TextView tvTagline = (TextView) findViewById(R.id.tvTagline);
        TextView tvFollowers = (TextView) findViewById(R.id.tvFollowers);
        TextView tvFollowing = (TextView) findViewById(R.id.tvFollowing);
        ImageView ivProfileImage = (ImageView) findViewById(R.id.ivProfileImage);

        tvName.setText(user.getName());
        tvTagline.setText(user.getTagline());
        tvFollowers.setText(String.valueOf(user.getFollowers()) + " " +  getString(R.string.followers));
        tvFollowing.setText(String.valueOf(user.getFollowing()) + " " + getString(R.string.following));

        ivProfileImage.setImageResource(android.R.color.transparent);
        Picasso.with(getContext()).load(user.getProfileImageUrl()).into(ivProfileImage);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    // Inflate the menu; this adds items to the action bar if it is present.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_timeline, menu);
        return true;
    }

    @Override
    public void onCloseTweetDetail() {
        // nothing to do here right now
    }

}
