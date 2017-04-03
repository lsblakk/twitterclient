package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterApplication;
import com.codepath.apps.twitterclient.TwitterClient;
import com.codepath.apps.twitterclient.activities.ProfileActivity;
import com.codepath.apps.twitterclient.adapters.TweetsArrayAdapter;
import com.codepath.apps.twitterclient.models.Tweet;
import com.codepath.apps.twitterclient.models.User;
import com.codepath.apps.twitterclient.utils.DividerItemDecoration;
import com.codepath.apps.twitterclient.utils.EndlessRecyclerViewScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.raizlabs.android.dbflow.sql.language.Condition;

import org.json.JSONArray;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;


/**
 * Created by lukas on 3/28/17.
 */

public abstract class TweetListFragment extends Fragment {

    private TweetsArrayAdapter adapter;
    private ArrayList<Tweet> tweets;
    private SwipeRefreshLayout swipeContainer;
    private EndlessRecyclerViewScrollListener scrollListener;
    private RecyclerView rvTweets;
    private TweetsArrayAdapter.OnItemClickListener onItemClickListener;

    SharedPreferences pref;
    SharedPreferences.Editor edit;

    public static TweetListFragment newInstance() {
        TweetListFragment frag = new TweetListFragment() {
            @Override
            protected void populateTimeline(int page) {

            }
        };

        return frag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Preference manager
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        edit = pref.edit();
        // reset these on opening the app
        edit.putLong("since_id", 1);
        edit.putLong("max_id", 1);

        tweets = new ArrayList<>();
        adapter = new TweetsArrayAdapter(getActivity(), tweets);
        onItemClickListener = new TweetsArrayAdapter.OnItemClickListener() {
            @Override
            public void loadProfileView(int position) {
                // reset maxID whenever clicking on a new user profile
                edit.putLong("max_id", 1);
                User user = tweets.get(position).getUser();
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra("user", Parcels.wrap(user));
                startActivity(intent);
            }
            @Override
            public void onItemClick(int position) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                TweetDetailFragment tweetDetailFragment = TweetDetailFragment.newInstance();
                Bundle args = new Bundle();
                args.putParcelable("tweet", Parcels.wrap(tweets.get(position)));
                tweetDetailFragment.setArguments(args);
                tweetDetailFragment.show(fm, "fragment_tweet_detail");

            }
        };
        adapter.setOnItemClickListener(onItemClickListener);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tweets_list, parent, false);

        // RecyclerView
        rvTweets = (RecyclerView) v.findViewById(R.id.rvTweets);
        rvTweets.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        layoutManager.scrollToPositionWithOffset(0,0);
        rvTweets.setLayoutManager(layoutManager);
        RecyclerView.ItemDecoration itemDecoration = new
                DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST);
        rvTweets.addItemDecoration(itemDecoration);

        // Swipe to refresh container
        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //populateTimeline(-1);
            }
        });


        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                populateTimeline(page);
            }
        };

        rvTweets.addOnScrollListener(scrollListener);

        return v;
    }

    // Abstract method to be overridden
    protected abstract void populateTimeline(int page);

    public void setPagination() {
        edit.putLong("max_id", tweets.get(tweets.size()-1).getUid());
        Log.d("Debug max_id", String.valueOf(tweets.get(tweets.size()-1).getUid()));
        edit.putLong("since_id", tweets.get(0).getUid());
        Log.d("Debug since_id", String.valueOf(tweets.get(0).getUid()));
        edit.commit();
    }

    public int getItemCount(){
        return adapter.getItemCount();
    }

    public void clear() { tweets.clear();}

    public void notifyDataSetChanged() { adapter.notifyDataSetChanged(); }

    public void addAllNew(JSONArray json) {
        adapter.clear();
        tweets.addAll(Tweet.fromJSONArray(json));
        adapter.addAll(tweets);
        //adapter = new TweetsArrayAdapter(getActivity(), tweets);
        rvTweets.setAdapter(adapter);
    }

    public void updateTweetList(JSONArray json) {
        tweets.addAll(Tweet.fromJSONArray(json));
        adapter.notifyItemRangeInserted(adapter.getItemCount(), tweets.size());
    }

    public void getOfflineTweets() {
        adapter.clear();
        tweets = Tweet.fetchDBTweets();
        adapter.addAll(tweets);
        rvTweets.setAdapter(adapter);
        // a visual for "offline" mode
        // Snackbar.make(findViewById(android.R.id.content), "Currently offline - no new tweets to show", Snackbar.LENGTH_LONG).show();
    }

    public void resetState() {
        scrollListener.resetState();
    }

    public void setRefreshing(Boolean refreshing){
        swipeContainer.setRefreshing(refreshing);
    }

    public void addTweet(Tweet newTweet) {
        if (!adapter.hasTweet(newTweet)){
            newTweet.save();
            adapter.addTweet(newTweet);
            adapter.notifyItemInserted(0);
            rvTweets.scrollToPosition(0);
        }
    }


}
