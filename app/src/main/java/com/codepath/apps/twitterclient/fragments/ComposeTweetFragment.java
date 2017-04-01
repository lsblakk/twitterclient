package com.codepath.apps.twitterclient.fragments;

import android.support.v4.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.twitterclient.R;
import com.codepath.apps.twitterclient.TwitterClient;

import java.util.ArrayList;

/**
 * Created by lukas on 3/22/17.
 */

public class ComposeTweetFragment extends DialogFragment {

    private TextView mComposeBody;
    private Button mTweetButton;
    private ComposeDialogListener listener;

    public interface ComposeDialogListener {
        void onFinishComposeDialog(String message);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ComposeDialogListener) {
            listener = (ComposeDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement ComposeTweetFragment.ComposeDialogListener");
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compose, container, false);

        mComposeBody = (EditText) view.findViewById(R.id.etComposeBody);
        mTweetButton = (Button) view.findViewById(R.id.btnTweet);
        mTweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFinishComposeDialog(mComposeBody.getText().toString());
                dismiss();
            }
        });

        return view;
    }

    public ComposeTweetFragment(){
        this.listener = null;
    }

    public static ComposeTweetFragment newInstance() {
        ComposeTweetFragment frag = new ComposeTweetFragment();

        return frag;
    }

}
