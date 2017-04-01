package com.codepath.apps.twitterclient.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.models.Tweet;
import com.squareup.picasso.Picasso;


import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;


/**
 * Created by lukas on 3/21/17.
 */

// Taking the Tweet objects and turning them into views
public class TweetsArrayAdapter extends RecyclerView.Adapter<TweetsArrayAdapter.ViewHolder> {

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView tvBody;
        public TextView tvUsername;
        public TextView tvName;
        public TextView tvTimestamp;
        public ImageView ivProfileImage;
        public IMyViewHolderClicks mListener;

        public ViewHolder(View itemView, IMyViewHolderClicks listener) {

            super(itemView);
            mListener = listener;
            tvBody = (TextView) itemView.findViewById(R.id.tvBody);
            tvBody.setOnClickListener(this);
            tvUsername = (TextView) itemView.findViewById(R.id.tvUsername);
            tvUsername.setOnClickListener(this);
            tvName = (TextView) itemView.findViewById(R.id.tvName);
            tvName.setOnClickListener(this);
            tvTimestamp = (TextView) itemView.findViewById(R.id.tvTimestamp);
            ivProfileImage = (ImageView) itemView.findViewById(R.id.ivProfileImage);
            ivProfileImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v instanceof ImageView){
                mListener.loadProfileView((ImageView)v);
            } else {
                mListener.loadTweetDetail(v);
            }
        }

        public interface IMyViewHolderClicks {
             void loadTweetDetail(View caller);
             void loadProfileView(ImageView callerImage);
        }
    }

    private List<Tweet> mTweets;
    private Context mContext;

    public TweetsArrayAdapter(Context context, List<Tweet> tweets) {
        mTweets = tweets;
        mContext = context;
    }

    private Context getContext() {
        return mContext;
    }

    @Override
    public TweetsArrayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View tweetView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(tweetView, new TweetsArrayAdapter.ViewHolder.IMyViewHolderClicks() {
            public void loadTweetDetail(View caller) {
                Log.d("DEBUG", "This will open the tweet detail Fragment");
            }
            public void loadProfileView(ImageView callerImage) {
                // open profile view
                Log.d("DEBUG", "This will open the profile view");
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TweetsArrayAdapter.ViewHolder holder, int position) {

        Tweet tweet = mTweets.get(position);

        TextView tvUsername = holder.tvUsername;
        tvUsername.setText(tweet.getUser().getScreenname());

        TextView tvName = holder.tvName;
        tvName.setText(tweet.getUser().getName());

        TextView tvBody = holder.tvBody;
        tvBody.setText(tweet.getBody());
        tvBody.setMinimumHeight(tvBody.getLineHeight());

        TextView tvTimestamp = holder.tvTimestamp;
        tvTimestamp.setText((tweet.getRelativeTimeAgo(tweet.getCreatedAt())));

        ImageView ivProfileImage = holder.ivProfileImage;
        ivProfileImage.setImageResource(android.R.color.transparent); // clear out old image
        Picasso.with(getContext()).load(tweet.getUser().getProfileImageUrl()).fit().transform(new RoundedCornersTransformation(10, 10)).into(ivProfileImage);

    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        mTweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items
    public void addAll(List<Tweet> tweets) {
        mTweets.addAll(tweets);
        notifyDataSetChanged();
    }
}
