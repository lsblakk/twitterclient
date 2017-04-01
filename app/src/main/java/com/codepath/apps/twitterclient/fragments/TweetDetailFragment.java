package com.codepath.apps.twitterclient.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import static com.codepath.apps.twitterclient.R.id.ivProfileImage;

/**
 * Created by lukas on 3/22/17.
 */

public class TweetDetailFragment extends DialogFragment {

    public TextView mBody;
    public TextView mUsername;
    public TextView mTimestamp;
    public ImageView mProfileImage;
    private TweetDetailsListener listener;

    // TODO - implement a "reply" button that triggers the ComposeTweetFragment with a M value (for reply to)

    public static TweetDetailFragment newInstance() {
        TweetDetailFragment frag = new TweetDetailFragment();

        return frag;
    }

    public interface TweetDetailsListener {
        void onCloseTweetDetail();
    }

    public void setTweetDetailListener(TweetDetailsListener listener){
        this.listener = listener;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof TweetDetailsListener) {
            listener = (TweetDetailsListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement TweetDetailsFragment.TweetDetailsListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_tweet_detail, container, false);

        Tweet tweet = Parcels.unwrap(getArguments().getParcelable("tweet"));
        mBody = (TextView) view.findViewById(R.id.tvBody);
        mBody.setText(tweet.getBody());

        mUsername = (TextView) view.findViewById(R.id.tvUsername);
        mUsername.setText(tweet.getUser().getScreenname());

        mTimestamp = (TextView) view.findViewById(R.id.tvTimestamp);
        mTimestamp.setText(tweet.getRelativeTimeAgo(tweet.getCreatedAt()));

        mProfileImage = (ImageView) view.findViewById(ivProfileImage);
        mProfileImage.setImageResource(android.R.color.transparent);
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).into(mProfileImage);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCloseTweetDetail();
            }
        });

        return view;
    }
}
