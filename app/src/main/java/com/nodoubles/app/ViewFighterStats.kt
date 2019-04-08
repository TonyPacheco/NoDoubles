package com.nodoubles.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.Fighter
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_view_fighter_stats.*
import java.util.*

class ViewFighterStats : AppCompatActivity() {

    private var fighterId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_fighter_stats)
        fighterId = intent.getIntExtra("fighter", -1)
        if(fighterId != -1)
            loadFighter()
        supportActionBar?.title = Html.fromHtml("<font color=\"#00202b\">" + getString(R.string.fighter_stats) + "</font>")


    }

    private fun loadFighter() {
        val ref = App.Globals.db.reference
                .child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighterId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val fighter = dataSnapshot.getValue(Fighter::class.java)
                initFields(fighter!!)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fighter.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    private fun initFields(f: Fighter){
        fighter_name.text = f.getFullName()
        if(!f.photoURL.isNullOrEmpty())
            Picasso.get().load(f.photoURL).into(fighter_img)
        fighter_stats.text = formatStats(f)
        head_g.text = f.headHitsGiven.toString()
        head_r.text = f.headHitsRecvd.toString()
        arms_g.text = f.armsHitsGiven.toString()
        arms_r.text = f.armsHitsRecvd.toString()
        hand_g.text = f.handHitsGiven.toString()
        hand_r.text = f.handHitsRecvd.toString()
        arms_g.text = f.armsHitsGiven.toString()
        arms_r.text = f.armsHitsRecvd.toString()
        body_g.text = f.bodyHitsGiven.toString()
        body_r.text = f.bodyHitsRecvd.toString()
        legs_g.text = f.legsHitsGiven.toString()
        legs_r.text = f.legsHitsRecvd.toString()
        clos_g.text = f.closedDbGiven.toString()
        clos_r.text = f.closedDbRecvd.toString()
        open_d.text = f.openDbExchngs.toString()
    }

    private fun formatStats(fighter: Fighter): String {
        val w = fighter.wins
        val l = fighter.losses
        val p = fighter.tourneyScore
        return String.format(Locale.CANADA,
                "%s%3d%n%s%3d%n%s%3d",
                getString(R.string.wins_abrev), w,
                getString(R.string.losses_abrev), l,
                getString(R.string.points_abrev), p)
    }
}
