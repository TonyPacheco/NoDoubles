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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
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
    private final Fighter[] toAlter = {null, null};
    private boolean safeGuardTripped = false;

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
                                deleteFight(fight);
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


    private void deleteFight (final Fight fight){
        resetPastFight(fight);
    }


    private void resetPastFight(final Fight fight){
        final String tourneyID = String.valueOf(App.Globals.INSTANCE.getTourneyID());
        
        final String fighter1ID = String.valueOf(fight.getFighter1().getId());
        DatabaseReference ref1 = App.Globals.db.getReference()
                .child("fighters")
                .child(tourneyID)
                .child(fighter1ID);
        ValueEventListener listener1 = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                toAlter[0] = dataSnapshot.getValue(Fighter.class);
                attemptDelete(fight);
            }
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to delete!",
                        Toast.LENGTH_SHORT).show();
            }
        };
        ref1.addListenerForSingleValueEvent(listener1);

        final String fighter2ID = String.valueOf(fight.getFighter2().getId());
        DatabaseReference ref2 = App.Globals.db.getReference()
                .child("fighters")
                .child(tourneyID)
                .child(fighter2ID);
        ValueEventListener listener2 = new ValueEventListener() {
            public void onDataChange(DataSnapshot dataSnapshot) {
                toAlter[1] = dataSnapshot.getValue(Fighter.class);
                attemptDelete(fight);
            }
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(context, "Failed to delete!",
                        Toast.LENGTH_SHORT).show();
            }
        };
        ref2.addListenerForSingleValueEvent(listener2);
    }

    private void attemptDelete(Fight fight) {
        if(toAlter[0] == null || toAlter[1] == null || safeGuardTripped)
            return;
        safeGuardTripped = true;
        final String tourneyID = String.valueOf(App.Globals.INSTANCE.getTourneyID());
        toAlter[0].setTourneyScore(toAlter[0].getTourneyScore() - fight.getFighter1Pts());
        toAlter[1].setTourneyScore(toAlter[1].getTourneyScore() - fight.getFighter2Pts());
        if(fight.getFighter1Pts() < fight.getFighter2Pts()){
            toAlter[1].removeWin();
            toAlter[0].removeLoss();
        } else if (fight.getFighter1Pts() > fight.getFighter2Pts()) {
            toAlter[0].removeWin();
            toAlter[1].removeLoss();
        } else {
            toAlter[0].removeLoss();
            toAlter[1].removeLoss();
        }
        App.Globals.db.getReference().child("fighters")
                .child(tourneyID)
                .child(String.valueOf(toAlter[0].getId()))
                .setValue(toAlter[0]);
        App.Globals.db.getReference().child("fighters")
                .child(tourneyID)
                .child(String.valueOf(toAlter[1].getId()))
                .setValue(toAlter[1]);
        App.Globals.db
                .getReference()
                .child("fights")
                .child(String.valueOf(App.Globals.INSTANCE.getTourneyID()))
                .child(String.valueOf(fight.getId()))
                .setValue(null);
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
