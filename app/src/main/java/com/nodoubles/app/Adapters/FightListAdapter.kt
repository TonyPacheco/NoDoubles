package com.nodoubles.app.Adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.App
import com.nodoubles.app.FightJudgeActivity
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import com.nodoubles.app.Models.Tourney
import com.nodoubles.app.R
import com.nodoubles.app.WeightedFightJudgeActivity
import com.squareup.picasso.Picasso
import java.util.*


@Suppress("DEPRECATION")
class FightListAdapter(private val context: Context, private val fights: ArrayList<Fight>) : RecyclerView.Adapter<FightListAdapter.CustomViewHolder>() {
    private val res: Resources = context.resources
    private val toAlter = arrayOf<Fighter?>(null, null)
    private var safeGuardTripped = false

    override fun getItemCount(): Int {
        return fights.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.fight_card, parent, false)
        return CustomViewHolder(v)
    }

    override fun onBindViewHolder(h: CustomViewHolder, i: Int) {
        val fight = fights[i]
        h.root.setOnClickListener {
            if (App.Globals.isAdmin) {
                val intent: Intent
                if (fight.status != Fight.STATUS_FINISHED) {
                    intent = if(App.Globals.tourney!!.scoreType == Tourney.SCORE_TYPE_REGION_BASED_SCORE)
                        Intent(context, WeightedFightJudgeActivity::class.java)
                    else
                        Intent(context, FightJudgeActivity::class.java)
                    intent.putExtra("fight", fight.id)
                    context.startActivity(intent)
                } else {
                    val alert = AlertDialog.Builder(context)
                    alert.setTitle(R.string.reset_fight_q)
                    alert.setMessage(context.getString(R.string.reset_fight_dialogue))
                    alert.setPositiveButton(R.string.RESET) { _, _ -> resetPastFight(fight, true) }
                    alert.setNegativeButton(R.string.CANCEL) { dialog, _ -> dialog.dismiss() }
                    alert.show()
                }
            }
        }
        h.root.setOnLongClickListener {
            if (App.Globals.isAdmin) {
                if (fight.status == Fight.STATUS_FIGHTING) {
                    Toast.makeText(context, R.string.cant_delete_in_prog, Toast.LENGTH_LONG).show()
                } else {
                    val alert = AlertDialog.Builder(context)
                    alert.setTitle(R.string.delete_q)
                    if (fight.status == Fight.STATUS_FINISHED) {
                        alert.setMessage(R.string.caution_fight_completed_delete)
                    } else {
                        alert.setMessage(R.string.caution)
                    }
                    alert.setPositiveButton(R.string.DELETE) { _, _ ->
                        if (fight.status == Fight.STATUS_FINISHED) {
                            resetPastFight(fight, false)
                        } else {
                            deleteFight(fight)
                        }
                    }
                    alert.setNegativeButton(R.string.CANCEL) { dialog, _ -> dialog.dismiss() }
                    alert.show()
                }
            }
            true
        }
        h.txtTop.text = generateTopText(fight)
        h.txtCtr.text = generateCtrText(fight)
        h.name1.text = fight.fighter1.firstName
        h.name2.text = fight.fighter2.firstName
        var url: String?
        url = fight.fighter1.photoURL
        if (url != null && !url.isEmpty())
            Picasso.get()
                    .load(url)
                    .placeholder(res.getDrawable(R.drawable.ic_sword_cross))
                    .error(res.getDrawable(R.drawable.ic_sword_cross))
                    .into(h.imgL)
        else
            h.imgL.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross))

        url = fight.fighter2.photoURL
        if (url != null && !url.isEmpty())
            Picasso.get()
                    .load(url)
                    .placeholder(res.getDrawable(R.drawable.ic_sword_cross))
                    .error(res.getDrawable(R.drawable.ic_sword_cross))
                    .into(h.imgR)
        else
            h.imgR.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross))
    }

    private fun resetPastFight(fight: Fight, recreate: Boolean) {
        val tourneyID = App.Globals.TourneyID.toString()

        val fighter1ID = fight.fighter1.id.toString()
        val ref1 = App.Globals.db.reference
                .child("fighters")
                .child(tourneyID)
                .child(fighter1ID)
        val listener1 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toAlter[0] = dataSnapshot.getValue(Fighter::class.java)!!
                attemptDelete(fight, recreate)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed to delete!",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref1.addListenerForSingleValueEvent(listener1)

        val fighter2ID = fight.fighter2.id.toString()
        val ref2 = App.Globals.db.reference
                .child("fighters")
                .child(tourneyID)
                .child(fighter2ID)
        val listener2 = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toAlter[1] = dataSnapshot.getValue(Fighter::class.java)!!
                attemptDelete(fight, recreate)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed to delete!",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref2.addListenerForSingleValueEvent(listener2)
    }

    private fun attemptDelete(fight: Fight, recreate: Boolean) {
        if (toAlter[0] == null || toAlter[1] == null || safeGuardTripped)
            return
        safeGuardTripped = true
        val tourneyID = App.Globals.TourneyID.toString()
        toAlter[0]!!.tourneyScore = toAlter[0]!!.tourneyScore - fight.fighter1Pts
        toAlter[1]!!.tourneyScore = toAlter[1]!!.tourneyScore - fight.fighter2Pts
        when {
            fight.fighter1Pts < fight.fighter2Pts -> {
                toAlter[1]!!.removeWin()
                toAlter[0]!!.removeLoss()
            }
            fight.fighter1Pts > fight.fighter2Pts -> {
                toAlter[0]!!.removeWin()
                toAlter[1]!!.removeLoss()
            }
            else -> {
                toAlter[0]!!.removeLoss()
                toAlter[1]!!.removeLoss()
            }
        }
        App.Globals.db.reference.child("fighters")
                .child(tourneyID)
                .child(toAlter[0]!!.id.toString())
                .setValue(toAlter[0])
        App.Globals.db.reference.child("fighters")
                .child(tourneyID)
                .child(toAlter[1]!!.id.toString())
                .setValue(toAlter[1])
        if (recreate)
            cloneFight(fight)
        deleteFight(fight)
    }


    private fun deleteFight(fight: Fight) {
        App.Globals.db
                .reference
                .child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fight.id.toString())
                .setValue(null)
    }

    private fun cloneFight(fight: Fight) {
        val newFight = Fight(fight.tourney,
                toAlter[0]!!, toAlter[1]!!,
                fight.matchType, fight.day, fight.time)
        App.Globals.db
                .reference
                .child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(newFight.id.toString())
                .setValue(newFight)
    }

    private fun generateTopText(f: Fight): String {
        val day = f.day
        val typ = f.matchType
        if (day == "")
            return if (typ == "")
                ""
            else
                typ
        return if (typ == "") day else "$day : $typ"
    }

    private fun generateCtrText(f: Fight): String {
        val status = f.status
        val time = f.time
        var text = ""
        if (status == Fight.STATUS_AWAITING)
            return if (time != "")
                time
            else
                context.getString(R.string.upcoming)
        if (status == Fight.STATUS_FINISHED) {
            text = context.getString(R.string.string_final) + "\n"
            text += f.fighter1Pts.toString() + " | " + f.fighter2Pts
        } else if (status == Fight.STATUS_FIGHTING) {
            text = f.fighter1.currentScore.toString() + " | " + f.fighter2.currentScore
        }
        return text
    }

    inner class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        var root: View = v.rootView
        var txtTop: TextView = v.findViewById(R.id.fight_top_bar)
        var txtCtr: TextView = v.findViewById(R.id.fight_center)
        var name1: TextView = v.findViewById(R.id.fighter1_name)
        var name2: TextView = v.findViewById(R.id.fighter2_name)
        var imgL: ImageView = v.findViewById(R.id.fighter_1_img)
        var imgR: ImageView = v.findViewById(R.id.fighter_2_img)

    }
}
