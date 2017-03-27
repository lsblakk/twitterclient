package com.codepath.apps.twitterclient.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.FloatingActionButton;
import android.widget.Toast;

import com.codepath.apps.twitterclient.MyDatabase;
import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.fragments.ComposeTweetFragment;
import com.codepath.apps.twitterclient.fragments.TweetDetailFragment;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.utils.DividerItemDecoration;
import com.codepath.apps.twitterclient.utils.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.utils.ItemClickSupport;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.structure.database.transaction.ProcessModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class TimelineActivity extends AppCompatActivity implements ComposeTweetFragment.ComposeDialogListener, TweetDetailFragment.TweetDetailsListener {

    private Boolean refresh;
    private TwitterClient client;
    private TweetsArrayAdapter adapter;
    private ArrayList<Tweet> tweets;
    private RecyclerView rvTweets;
    private EndlessRecyclerViewScrollListener scrollListener;
    private SwipeRefreshLayout swipeContainer;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        // Preference manager
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        edit = pref.edit();
        // reset these on opening the app
        edit.putLong("since_id", 1);
        edit.putLong("max_id", 1);

        tweets = new ArrayList<>();
        adapter = new TweetsArrayAdapter(this, tweets);

        // RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPositionWithOffset(0,0);
        rvTweets.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);

        // Swipe to refresh container
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateTimeline(-1);
            }
        });

        // hook up item click for RecyclerView
        ItemClickSupport.addTo(rvTweets).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        FragmentManager fm = getSupportFragmentManager();
                        TweetDetailFragment tweetDetailFragment = TweetDetailFragment.newInstance();
                        Bundle args = new Bundle();
                        args.putParcelable("tweet", Parcels.wrap(tweets.get(position)));
                        tweetDetailFragment.setArguments(args);
                        tweetDetailFragment.show(fm, "fragment_tweet_detail");
                    }
                }
        );

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                populateTimeline(page);
            }
        };
        // Add the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        // Set a client for making API calls
        client = TwitterApplication.getRestClient();

        // Compose button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetFragment composeTweetFragment = ComposeTweetFragment.newInstance();
                composeTweetFragment.show(fm, "fragment_compose");
            }
        });

        // Get the initial homefeed on load
        populateTimeline(0);
    }

    private void setPagination() {
        edit.putLong("max_id", tweets.get(tweets.size()-1).getUid());
        Log.d("Debug max_id", String.valueOf(tweets.get(tweets.size()-1).getUid()));
        edit.putLong("since_id", tweets.get(0).getUid());
        Log.d("Debug since_id", String.valueOf(tweets.get(0).getUid()));
        edit.commit();
    }

    // Send an API request to get timeline jason
    // Fill the RecyclerView by creating the tweet objects
    private void populateTimeline(int page){

        if (isNetworkAvailable() && isOnline()) {

            if (page == -1) {
                refresh = true;
            } else {
                refresh = false;
            }

            client.getHomeTimeline(page, new JsonHttpResponseHandler() {
                // SUCCESS
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray json) {

                    int curSize = adapter.getItemCount();

                    if (curSize == 0 || refresh) {
                        // 1. First, clear the array of data & clean out the DB
                        tweets.clear();
                        // Delete the tables
                        Delete.tables(Tweet.class, User.class);
                        // 2. Notify the adapter of the update
                        adapter.notifyDataSetChanged(); // or notifyItemRangeRemoved
                        // 3. Reset endless scroll listener when performing a new search
                        scrollListener.resetState();
                        // Get new tweets
                        tweets.addAll(Tweet.fromJSONArray(json));
                        adapter = new TweetsArrayAdapter(getApplicationContext(), tweets);
                        rvTweets.setAdapter(adapter);
                    } else {
                        tweets.addAll(Tweet.fromJSONArray(json));
                        adapter.notifyItemRangeInserted(curSize, tweets.size());
                    }
                    setPagination();
                    swipeContainer.setRefreshing(false);
                }

                // FAILURE
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
        } else {
            // get tweets from DB to populate the home screen
            tweets = Tweet.fetchDBTweets();
            adapter = new TweetsArrayAdapter(getApplicationContext(), tweets);
            rvTweets.setAdapter(adapter);
            // a visual for "offline" mode
            Snackbar.make(findViewById(android.R.id.content), "Currently offline - no new tweets to show", Snackbar.LENGTH_INDEFINITE).show();
        }
    }

    @Override
    public void onFinishComposeDialog(String message) {
        // TODO handle the new tweet coming back from compose here, put up a Toast for submitted
        if (message.isEmpty()){
            Toast.makeText(this, "Can't tweet nothing!", Toast.LENGTH_LONG).show();
        } else {
            client.composeTweet(message, new JsonHttpResponseHandler () {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject json) {
                    Toast.makeText(getApplicationContext(), getString(R.string.tweet_success), Toast.LENGTH_LONG).show();
                    // Insert the tweet into rvTweets and scroll to top
                    Tweet tweet = Tweet.fromJSON(json);
                    tweets.add(0, tweet);
                    adapter.notifyItemInserted(0);
                    rvTweets.scrollToPosition(0);
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Log.d("DEBUG", errorResponse.toString());
                }
            });
        }
    }
    @Override
    public void onCloseTweetDetail() {
        // nothing to do here right now
    }

    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }
        return false;
    }

}
