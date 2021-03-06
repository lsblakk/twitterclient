package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.Delete;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import cz.msebera.android.httpclient.Header;

/**
 * Created by lukas on 3/28/17.
 */

public class MentionsTimelineFragment extends TweetListFragment {

    private Boolean refresh;
    private TwitterClient client;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set a client for making API calls
        client = TwitterApplication.getRestClient();

        // Get the initial homefeed on load
        populateTimeline(0);
    }

    // Send an API request to get timeline jason
    // Fill the RecyclerView by creating the tweet objects
    @Override
    public void populateTimeline(int page){
            if (page == -1) {
                refresh = true;
            } else {
                refresh = false;
            }

            client.getMentionsTimeline(page, new JsonHttpResponseHandler() {
                // SUCCESS
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray json) {

                    int curSize = getItemCount();
                    if (curSize == 0 || refresh) {
                        // 1. First, clear the array of data & clean out the DB
                        clear();
                        // Delete the tables
                        Delete.tables(Tweet.class, User.class);
                        // 2. Notify the adapter of the update
                        notifyDataSetChanged(); // or notifyItemRangeRemoved
                        // 3. Reset endless scroll listener when performing a new search
                        resetState();
                        // Get new tweets
                        addAllNew(json);
                    } else {
                        updateTweetList(json);
                    }
                    setPagination();
                    setRefreshing(false);
                }

                // FAILURE
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
    }



}
