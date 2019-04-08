package com.nodoubles.app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.Tourney
import kotlinx.android.synthetic.main.activity_organize_tournament.*
import java.net.URLConnection
import com.nodoubles.app.R

class OrganizeTournament : AppCompatActivity() {

    private var editingExisting = false
    private var ref = App.Globals.db.reference
    private var tournament: Tourney? = null

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

        val selectedScoring = if(radio_group_scoring.checkedRadioButtonId == radio_default.id)
            Tourney.SCORE_TYPE_STANDARD_WEIGHTING
        else
            Tourney.SCORE_TYPE_REGION_BASED_SCORE

        val tournament = Tourney(name,
                App.Globals.auth.currentUser!!.uid,
                if(isImage) url else "",
                selectedScoring)

        App.Globals.db.reference.child("tournaments")
                                .child(tournament.id.toString())
                                .setValue(tournament)

        App.Globals.TourneyID = tournament.id
        App.Globals.tourney = tournament
        App.Globals.isAdmin = true
        startActivity(Intent(this, ViewRosterActivity::class.java))
        finish()
    }
}
