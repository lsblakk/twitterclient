package com.codepath.apps.twitterclient.activities;

import android.support.v4.app.FragmentManager;
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

    private TwitterClient client;
    private TweetsArrayAdapter adapter;
    private ArrayList<Tweet> tweets;
    private RecyclerView rvTweets;
    private EndlessRecyclerViewScrollListener scrollListener;

    // TODO - make links in tweets open in a ChromeTab

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        rvTweets = (RecyclerView) findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();
        adapter = new TweetsArrayAdapter(this, tweets);
        rvTweets.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPosition(0);
        rvTweets.setLayoutManager(layoutManager);
        // For increased performance
        rvTweets.setHasFixedSize(true);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);

        // hook up click for RecyclerView
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
                loadNextDataFromApi(page);
            }
        };
        // Add the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        client = TwitterApplication.getRestClient();
        populateTimeline();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO launch a DialogFragment to compose a tweet with
                FragmentManager fm = getSupportFragmentManager();
                ComposeTweetFragment composeTweetFragment = ComposeTweetFragment.newInstance();
                composeTweetFragment.show(fm, "fragment_compose");
                // TODO Refresh the timeline to show this new tweet
            }
        });
    }

    // Append the next page of data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
        // TODO call populateTimeline() with new page
    }

    // Send an API request to get timeline jason
    // Fill the listview by creating the tweet objects
    private void populateTimeline(){

        // TODO - be able to call getHomeTimeline with a page value
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            // SUCCESS
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray json) {

                int curSize = adapter.getItemCount();

                tweets.addAll(Tweet.fromJSONArray(json));

                if (curSize == 0){
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
                    adapter.notifyItemRangeInserted(curSize, tweets.size());
                }
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
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
    @Override
    public void onCloseTweetDetail() {
        // TODO handle returning from tweet detail view (might be nothing to do here)
    }

}
