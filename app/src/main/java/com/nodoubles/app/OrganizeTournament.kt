package com.nodoubles.app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.ScoreScheme
import com.nodoubles.app.Models.Tourney
import kotlinx.android.synthetic.main.activity_organize_tournament.*
import java.net.URLConnection
import com.nodoubles.app.R
import kotlinx.android.synthetic.main.activity_view_fighter_stats.*

class OrganizeTournament : AppCompatActivity() {

    private var editingExisting = false
    private var ref = App.Globals.db.reference
    private var tournament: Tourney? = null
    private var defaultScoring = true

    private fun getTourneyFromFb() {
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tournament = dataSnapshot.getValue(Tourney::class.java)
                editTourneyName.setText(tournament!!.name)
                editTourneyPhotoURL.setText(tournament?.photoURL)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load tournament.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organize_tournament)
        val id = intent.getIntExtra("tourney", -1)
        if(id != -1)
            initFields(id)
        supportActionBar?.title = if(editingExisting) getString(R.string.edit_tourney) else getString(R.string.new_tourney)
        radio_default.setOnClickListener{
            defaultScoring = true
            toggleValuesVisibility(View.INVISIBLE)
        }
        radio_weighted.setOnClickListener{
            defaultScoring = false
            toggleValuesVisibility(View.VISIBLE)
        }
    }

    private fun initFields(id: Int){
        ref = ref.child("tournaments").child(id.toString())
        getTourneyFromFb()
        editingExisting = true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.save -> {
                saveTournamentNew()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveTournamentNew(){
        val name = editTourneyName.text.toString()
        val url: String = editTourneyPhotoURL.text.toString()
        var isImage = false
        try {
            val mimeType = URLConnection.guessContentTypeFromName(url)
            isImage = mimeType != null && mimeType.startsWith("image")
        } catch (e: Exception) { }

        if(defaultScoring){
            tournament = Tourney(name,
                    App.Globals.auth.currentUser!!.uid,
                    if(isImage) url else "",
                    Tourney.SCORE_TYPE_STANDARD_WEIGHTING)

            App.Globals.db.reference.child("tournaments")
                    .child(tournament!!.id.toString())
                    .setValue(tournament)
        } else {

            val head = if(!empty(input_head))     Integer.valueOf(input_head.text.toString())     else 0
            val body = if(!empty(input_body))     Integer.valueOf(input_body.text.toString())     else 0
            val arms = if(!empty(input_arms))     Integer.valueOf(input_arms.text.toString())     else 0
            val hand = if(!empty(input_hands))    Integer.valueOf(input_hands.text.toString())    else 0
            val legs = if(!empty(input_legs))     Integer.valueOf(input_legs.text.toString())     else 0
            val clsg = if(!empty(input_closed_g)) Integer.valueOf(input_closed_g.text.toString()) else 0
            val clsr = if(!empty(input_closed_r)) Integer.valueOf(input_closed_r.text.toString()) else 0
            val open = if(!empty(input_open))     Integer.valueOf(input_open.text.toString())     else 0

            val scheme = ScoreScheme(head,body,arms,hand,legs,clsg,clsr,open)

            App.Globals.db.reference.child("schemes")
                    .child(scheme.id)
                    .setValue(scheme)

            tournament = Tourney(name,
                    App.Globals.auth.currentUser!!.uid,
                    if(isImage) url else "",
                    Tourney.SCORE_TYPE_REGION_BASED_SCORE,
                    scheme.id,
                    input_track_hits.isChecked)
            App.Globals.db.reference.child("tournaments")
                    .child(tournament!!.id.toString())
                    .setValue(tournament)
        }

        App.Globals.TourneyID = tournament!!.id
        App.Globals.tourney = tournament
        App.Globals.isAdmin = true
        startActivity(Intent(this, ViewRosterActivity::class.java))
        finish()
    }

    private fun empty (inp: EditText) : Boolean {
        return inp.text.isNullOrEmpty()
    }

    private fun toggleValuesVisibility(vis: Int){
        values_header.visibility = vis
        input_head.visibility = vis
        input_body.visibility = vis
        input_arms.visibility = vis
        input_hands.visibility = vis
        input_legs.visibility = vis
        input_track_hits.visibility = vis
        head_label.visibility = vis
        body_label.visibility = vis
        arm_label.visibility = vis
        hand_label.visibility = vis
        leg_label.visibility = vis
        input_open.visibility = vis
        input_closed_g.visibility = vis
        input_closed_r.visibility = vis
        closed_label_g.visibility = vis
        closed_label_r.visibility = vis
        open_label.visibility = vis

    }

}
