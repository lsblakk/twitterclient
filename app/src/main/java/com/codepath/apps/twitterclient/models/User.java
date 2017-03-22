package com.codepath.apps.twitterclient.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukas on 3/21/17.
 */
/*

"user": {
    "name": "Jason Costa",
    "profile_sidebar_border_color": "86A4A6",
    "profile_sidebar_fill_color": "A0C5C7",
    "profile_background_tile": false,
    "profile_image_url": "http://a0.twimg.com/profile_images/1751674923/new_york_beard_normal.jpg",
    "created_at": "Wed May 28 00:20:15 +0000 2008",
    "location": "",
    "is_translator": true,
    "follow_request_sent": false,
    "id_str": "14927800",
    "profile_link_color": "FF3300",

 */

public class User {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getScreenname() {
        return screenname;
    }

    public void setScreenname(String screenname) {
        this.screenname = screenname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    private String name;
    private long uid;
    private String screenname;
    private String profileImageUrl;


    public static User fromJSON(JSONObject json){
        User u = new User();
        try {
            u.name = json.getString("name");
            u.uid = json.getLong("id");
            u.screenname = json.getString("screen_name");
            u.profileImageUrl = json.getString("profile_image_url_https");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return u;
    }

}
