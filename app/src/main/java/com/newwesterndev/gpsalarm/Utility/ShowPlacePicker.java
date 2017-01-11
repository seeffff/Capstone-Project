package com.newwesterndev.gpsalarm.utility;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import com.newwesterndev.gpsalarm.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ShowPlacePicker extends Dialog {

    @BindView(R.id.suggestion_list) ListView suggestionList;
    @BindView(R.id.cancel_button) Button cancelButton;
    private Context mContext;
    private ArrayList<PlaceSuggestion> mPlaceSuggestionList;
    private PlaceSuggestionListAdapter mPlaceSuggestionAdapter;

    public ShowPlacePicker(Context context, ArrayList<PlaceSuggestion> suggestions){
        super(context);
        mPlaceSuggestionList = suggestions;
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_content);
        ButterKnife.bind(this);

        mPlaceSuggestionAdapter = new PlaceSuggestionListAdapter(mContext, mPlaceSuggestionList);
        suggestionList.setAdapter(mPlaceSuggestionAdapter);

        cancelButton.setOnClickListener(view -> ShowPlacePicker.this.dismiss());
    }


}
