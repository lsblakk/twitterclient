package com.codepath.apps.twitterclient;

import org.scribe.builder.api.Api;
import org.scribe.builder.api.FlickrApi;
import org.scribe.builder.api.TwitterApi;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class TwitterClient extends OAuthBaseClient {

	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = "nmuR6t2nLA2Zb65Uz5imCGyrb";
	public static final String REST_CONSUMER_SECRET = "xiZrv3WgbIS8fYDPQvELp9lpR2x1Rt3f3ODKaAFvgaPibjTjLq";
	public static final String REST_CALLBACK_URL = "oauth://cptwitterclient"; // Change this (here and in manifest)

    SharedPreferences pref =
            PreferenceManager.getDefaultSharedPreferences(context);

    public TwitterClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

    // HomeTimeline - Gets us the home timeline data
	public void getHomeTimeline(int page, AsyncHttpResponseHandler handler){
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
        // A "page" will be 25 tweets
        params.put("count", 50);

        // Use since_id to hold the processed tweets and max_id to hold the
        long maxId = pref.getLong("max_id", 1);
        if (page == -1){
            // Refresh want the newest tweets
            params.put("since_id", 1);
        }
        if (page > 0) {
            params.put("max_id", maxId);
        }
		// Execute the request
		client.get(apiUrl, params, handler);
	}

    public void composeTweet(String message, AsyncHttpResponseHandler handler) {

        String apiURL = getApiUrl("statuses/update.json");
        RequestParams params = new RequestParams();
        params.put("status", message);
        client.post(apiURL, params, handler);

    }

}
