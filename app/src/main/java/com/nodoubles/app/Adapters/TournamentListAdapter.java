package com.nodoubles.app.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodoubles.app.App;
import com.nodoubles.app.ImageDLTask;
import com.nodoubles.app.Models.Tourney;
import com.nodoubles.app.R;
import com.nodoubles.app.ViewRosterActivity;


import java.util.ArrayList;

public class TournamentListAdapter extends RecyclerView.Adapter<TournamentListAdapter.CustomViewHolder> {

    public int getItemCount() {return tourneys.size();}
    private Context context;
    private ArrayList<Tourney> tourneys;

    public TournamentListAdapter(Context context, ArrayList<Tourney> tourneys){
        this.tourneys = tourneys;
        this.context = context;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tournament, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder h, int i) {
        final Tourney tourney = tourneys.get(i);
        h.title.setText(tourney.getName());
        h.root.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                App.Globals.INSTANCE.setTourneyID(tourney.getId());
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putInt("tourneyID", tourney.getId());
                editor.apply();
                App.Globals.INSTANCE.setAdmin(App.Globals.INSTANCE.isLoggedIn()
                        && tourney.getOrganizers().contains(App.Globals.auth.getCurrentUser().getUid()));
                Intent intent = new Intent(context, ViewRosterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK| Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
        String url = tourney.getPhotoURL();
        if(url!= null && !url.equals(""))
            new ImageDLTask(h.photo).execute(url);
        else
            h.photo.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_sword_cross));
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        View root;
        TextView title;
        ImageView photo;

        CustomViewHolder(View v) {
            super(v);
            title = v.findViewById(R.id.title);
            photo = v.findViewById(R.id.tourney_photo);
            root = v.getRootView();
        }

    }

}
