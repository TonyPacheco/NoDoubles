package com.nodoubles.app.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodoubles.app.App;
import com.nodoubles.app.EditFighterActivity;
import com.nodoubles.app.Models.Fighter;
import com.nodoubles.app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Locale;


public class RosterAdapter extends RecyclerView.Adapter<RosterAdapter.CustomViewHolder> {

    public int getItemCount() {return fighters.size();}
    private Context context;
    private ArrayList<Fighter> fighters;
    private Resources res;

    public RosterAdapter(Context context, ArrayList<Fighter> fighters){
        this.fighters = fighters;
        this.context = context;
        res = context.getResources();

    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fighter_card, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder h, int i) {
        final Fighter fighter = fighters.get(i);
        h.root.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if(App.Globals.INSTANCE.isAdmin()){
                    Intent i = new Intent(context, EditFighterActivity.class);
                    i.putExtra("fighter", fighter.getId());
                    context.startActivity(i);
                }
            }
        });
        h.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(App.Globals.INSTANCE.isAdmin()) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle(R.string.delete_q);
                    alert.setMessage(R.string.delete_warning);
                    alert.setPositiveButton(R.string.DELETE, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //deleteFighter(fighter);
                        }
                    });
                    alert.setNegativeButton(R.string.CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alert.show();
                }
                return false;
            }
        });
        h.name.setText(fighter.getFullName()) ;
        h.stats.setText(formatStats(fighter));
        String url = fighter.getPhotoURL();
        if(url != null && !url.equals(""))
            Picasso.get()
                    .load(url)
                    .placeholder(res.getDrawable(R.drawable.ic_sword_cross))
                    .error(res.getDrawable(R.drawable.ic_sword_cross))
                    .into(h.profile);
        else
            h.profile.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross));

    }

    private String formatStats(Fighter f){
        int w = f.getWins();
        int l = f.getLosses();
        int p = f.getTourneyScore();
        return String.format(Locale.CANADA,
                "%s%3d%n%s%3d%n%s%3d",
                context.getString(R.string.wins_abrev)  ,w,
                context.getString(R.string.losses_abrev),l,
                context.getString(R.string.points_abrev),p);
    }

//    private void deleteFighter (final Fighter fighter){
//        RealmConfiguration config = SyncUser.current()
//                .createConfiguration(App.Globals.INSTANCE_ADDRESS + App.Globals.REALM)
//                .build();
//        Realm.getInstance(config).executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                ArrayList<Fight> res2 = realm
//                        .where(Fight.class)
//                        .equalTo("fighter1.id", fighter.getId())
//                        .or()
//                        .equalTo("fighter2.id", fighter.getId())
//                        .findAll();
//                res2.deleteAllFromRealm();
//                ArrayList<Fighter> res = realm
//                        .where(Fighter.class)
//                        .equalTo("id", fighter.getId())
//                        .findAll();
//                res.deleteAllFromRealm();
//            }
//        });
//    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        View root;
        ImageView profile;
        TextView name, stats;

        CustomViewHolder(View v) {
            super(v);
            root = v.getRootView();
            profile = v.findViewById(R.id.fighter_img);
            name = v.findViewById(R.id.fighter_name);
            stats = v.findViewById(R.id.fighter_stats);
        }
    }

}
