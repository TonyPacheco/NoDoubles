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
import android.widget.Toast;

import com.nodoubles.app.App;
import com.nodoubles.app.FightJudgeActivity;
import com.nodoubles.app.ImageDLTask;
import com.nodoubles.app.Models.*;
import com.nodoubles.app.R;

import java.util.ArrayList;


public class FightListAdapter extends RecyclerView.Adapter<FightListAdapter.CustomViewHolder> {

    public int getItemCount() {return fights.size();}
    private Context context;
    private ArrayList<Fight> fights;
    private Resources res;

    public FightListAdapter(Context context, ArrayList<Fight> fights){
        this.fights = fights;
        this.context = context;
        res = context.getResources();
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fight_card, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder h, int i) {
        final Fight fight = fights.get(i);
        h.root.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view) {
                if(App.Globals.INSTANCE.isAdmin()) {
                    if(fight.getStatus() != Fight.Companion.getSTATUS_FINISHED()) {
                        Intent i = new Intent(context, FightJudgeActivity.class);
                        i.putExtra("fight", fight.getId());
                        context.startActivity(i);
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle(R.string.reset_fight_q);
                        alert.setMessage(context.getString(R.string.reset_fight_dialogue));
                        alert.setPositiveButton(R.string.RESET, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                resetPastFight(fight);
                                Intent i = new Intent(context, FightJudgeActivity.class);
                                i.putExtra("fight", fight.getId());
                                context.startActivity(i);
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
                }
            }
        });
        h.root.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(App.Globals.INSTANCE.isAdmin()) {
                    if (fight.getStatus() == Fight.Companion.getSTATUS_FIGHTING()) {
                        Toast.makeText(context, R.string.cant_delete_in_prog, Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder alert = new AlertDialog.Builder(context);
                        alert.setTitle(R.string.delete_q);
                        if(fight.getStatus() == Fight.Companion.getSTATUS_FINISHED()){
                            alert.setMessage(R.string.caution_fight_completed_delete);
                        } else {
                            alert.setMessage(R.string.caution);
                        }
                        alert.setPositiveButton(R.string.DELETE, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(fight.getStatus() == Fight.Companion.getSTATUS_FINISHED()){
                                    resetPastFight(fight);
                                }
                                //deleteFight(fight);
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
                }
                return true;
            }
        });
        h.txtTop.setText(generateTopText(fight));
        h.txtCtr.setText(generateCtrText(fight));
        h.name1.setText(fight.getFighter1().getFirstName());
        h.name2.setText(fight.getFighter2().getFirstName());
        String url;
        if((url = fight.getFighter1().getPhotoURL()) != null && !url.equals(""))
            new ImageDLTask(h.imgL).execute(url);
        else
            h.imgL.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross));
        if((url = fight.getFighter2().getPhotoURL()) != null && !url.equals(""))
            new ImageDLTask(h.imgR).execute(url);
        else
            h.imgR.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross));
    }


//    private void deleteFight (final Fight fight){
//        RealmConfiguration config = SyncUser.current()
//                .createConfiguration(App.Globals.INSTANCE_ADDRESS + App.Globals.REALM)
//                .build();
//        Realm.getInstance(config).executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                ArrayList<Fight> res = realm
//                        .where(Fight.class)
//                        .equalTo("id", fight.getId())
//                        .findAll();
//                res.deleteAllFromRealm();
//            }
//        });
//    }


    private void resetPastFight(final Fight fight){
        final Fighter fighter1 = fight.getFighter1();
        final Fighter fighter2 = fight.getFighter2();
        fighter1.setTourneyScore(fighter1.getTourneyScore() - fight.getFighter1Pts());
        fighter2.setTourneyScore(fighter2.getTourneyScore() - fight.getFighter2Pts());
        if(fight.getFighter1Pts() < fight.getFighter2Pts()){
            fighter2.removeWin();
            fighter1.removeLoss();
        } else if (fight.getFighter1Pts() > fight.getFighter2Pts()) {
            fighter1.removeWin();
            fighter2.removeLoss();
        } else {
            fighter1.removeLoss();
            fighter2.removeLoss();
        }
        App.Globals.db.getReference().child("fighters")
                .child(String.valueOf(fighter1.getId()))
                .setValue(fighter1);
        App.Globals.db.getReference().child("fighters")
                .child(String.valueOf(fighter2.getId()))
                .setValue(fighter2);
    }

    private String generateTopText(Fight f){
        String day = f.getDay();
        String typ = f.getMatchType();
        if(day.equals(""))
            if(typ.equals(""))
                return "";
            else return typ;
        if(typ.equals(""))
            return day;
        return day + " : " + typ;
    }

    private String generateCtrText(Fight f){
        int status = f.getStatus();
        String time = f.getTime();
        String text = "";
        if(status == Fight.Companion.getSTATUS_AWAITING())
            if(!time.equals(""))
                return time;
            else return context.getString(R.string.upcoming);
        if(status == Fight.Companion.getSTATUS_FINISHED()) {
            text = context.getString(R.string.string_final) + "\n";
            text += f.getFighter1Pts() + " | " + f.getFighter2Pts();
        } else if (status == Fight.Companion.getSTATUS_FIGHTING()) {
            text = f.getFighter1().getCurrentScore() + " | " + f.getFighter2().getCurrentScore();
        }
        return text;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {

        View root;
        TextView txtTop, txtCtr, name1, name2;
        ImageView imgL, imgR;

        CustomViewHolder(View v) {
            super(v);
            root = v.getRootView();
            txtTop = v.findViewById(R.id.fight_top_bar);
            txtCtr = v.findViewById(R.id.fight_center);
            name1 = v.findViewById(R.id.fighter1_name);
            name2 = v.findViewById(R.id.fighter2_name);
            imgL = v.findViewById(R.id.fighter_1_img);
            imgR = v.findViewById(R.id.fighter_2_img);
        }

    }
}
