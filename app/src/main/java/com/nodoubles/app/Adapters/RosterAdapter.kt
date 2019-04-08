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
import com.nodoubles.app.EditFighterActivity
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import com.nodoubles.app.R
import com.squareup.picasso.Picasso

import java.util.ArrayList
import java.util.Locale


class RosterAdapter(private val context: Context, private val fighters: ArrayList<Fighter>) : RecyclerView.Adapter<RosterAdapter.CustomViewHolder>() {
    private val res: Resources = context.resources

    override fun getItemCount(): Int {
        return fighters.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.fighter_card, parent, false)
        return CustomViewHolder(v)
    }

    override fun onBindViewHolder(h: CustomViewHolder, i: Int) {
        val fighter = fighters[i]
        h.root.setOnClickListener {
            if (App.Globals.isAdmin) {
                val i = Intent(context, EditFighterActivity::class.java)
                i.putExtra("fighter", fighter.id)
                context.startActivity(i)
            }
        }
        h.root.setOnLongClickListener {
            if (App.Globals.isAdmin) {
                val alert = AlertDialog.Builder(context)
                alert.setTitle(R.string.delete_q)
                alert.setMessage(R.string.delete_warning)
                alert.setPositiveButton(R.string.DELETE) { _, _ -> deleteFighter(fighter) }
                alert.setNegativeButton(R.string.CANCEL) { dialog, _ -> dialog.dismiss() }
                alert.show()
            }
            false
        }
        h.name.text = fighter.getFullName()
        h.stats.text = formatStats(fighter)
        val url = fighter.photoURL
        if (url != null && url != "")
            Picasso.get()
                    .load(url)
                    .placeholder(res.getDrawable(R.drawable.ic_sword_cross))
                    .error(res.getDrawable(R.drawable.ic_sword_cross))
                    .into(h.profile)
        else
            h.profile.setImageDrawable(res.getDrawable(R.drawable.ic_sword_cross))

    }

    private fun formatStats(f: Fighter): String {
        val w = f.wins
        val l = f.losses
        val p = f.tourneyScore
        return String.format(Locale.CANADA,
                "%s%3d%n%s%3d%n%s%3d",
                context.getString(R.string.wins_abrev), w,
                context.getString(R.string.losses_abrev), l,
                context.getString(R.string.points_abrev), p)
    }

    private fun deleteFighter(fighter: Fighter) {
        var fightsDeleted = 0

        val ref = App.Globals.db.reference.child("fights").child(App.Globals.TourneyID.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    val fight = postSnapshot.getValue(Fight::class.java)
                    if(fight?.status != Fight.STATUS_FINISHED
                            && (fight?.fighter1?.id == fighter.id || fight?.fighter2?.id == fighter.id)){
                        deleteFight(fight)
                        ++fightsDeleted
                    }
                }

                App.Globals.db
                        .reference
                        .child("fighters")
                        .child(App.Globals.TourneyID.toString())
                        .child(fighter.id.toString())
                        .setValue(null)

                if(fightsDeleted != 0)
                    Toast.makeText(context, "$fightsDeleted fights deleted.",
                        Toast.LENGTH_SHORT).show()

            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(context, "Failed delete fighter.",
                        Toast.LENGTH_SHORT).show()
            }
        }

        ref.addValueEventListener(listener)
    }

    private fun deleteFight(fight: Fight) {
        App.Globals.db
                .reference
                .child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fight.id.toString())
                .setValue(null)
    }

    inner class CustomViewHolder(v: View) : RecyclerView.ViewHolder(v) {

        var root: View = v.rootView
        var profile: ImageView = v.findViewById(R.id.fighter_img)
        var name: TextView = v.findViewById(R.id.fighter_name)
        var stats: TextView = v.findViewById(R.id.fighter_stats)

    }

}
