package com.codepath.apps.twitterclient.models;

import com.codepath.apps.twitterclient.MyDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

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
@Table(database = MyDatabase.class)
@Parcel(analyze={User.class})
public class User extends BaseModel {
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenname() {
        return screenname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    // empty constructor needed by the Parceler library
    public User() {}

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    @Column
    public String name;

    @Column
    @PrimaryKey
    public long uid;

    @Column
    public String screenname;

    @Column
    public String profileImageUrl;


    public static User fromJSON(JSONObject json){
        User u = new User();
        try {
            u.name = json.getString("name");
            u.uid = json.getLong("id");
            u.screenname = "(@" + json.getString("screen_name") + ")";
            u.profileImageUrl = json.getString("profile_image_url_https");
            u.save();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return u;
    }

}
