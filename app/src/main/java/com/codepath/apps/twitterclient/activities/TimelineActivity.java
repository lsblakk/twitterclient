package com.codepath.apps.twitterclient.activities;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.fragments.ComposeTweetFragment;
import com.codepath.apps.twitterclient.fragments.TweetDetailFragment;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.utils.DividerItemDecoration;
import com.codepath.apps.twitterclient.utils.EndlessRecyclerViewScrollListener;
import com.codepath.apps.twitterclient.utils.ItemClickSupport;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

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
        edit.putLong("since_id", 1);
        edit.putLong("max_id", 1);

        tweets = new ArrayList<>();
        adapter = new TweetsArrayAdapter(this, tweets);

        // RecyclerView
        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        rvTweets.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPosition(0);
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

        if (page == -1){
            refresh = true;
        } else {
            refresh = false;
        }

        client.getHomeTimeline(page, new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {

                int curSize = adapter.getItemCount();

                if (curSize == 0 || refresh){
                    // 1. First, clear the array of data
                    tweets.clear();
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
                    // Insert the tweet into rvTweets
                    int curSize = adapter.getItemCount();
                    Tweet tweet = Tweet.fromJSON(json);
                    tweets.add(tweet);
                    adapter.notifyItemRangeInserted(curSize, tweets.size());
                    // Set scroll position to 0
                    rvTweets.scrollTo(0,0);
                    // refresh timeline here to show the new tweet
                    //populateTimeline(-1);
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

}
