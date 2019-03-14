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
import com.nodoubles.app.Models.Fighter
import kotlinx.android.synthetic.main.activity_edit_fighter.*
import java.net.URLConnection

class EditFighterActivity : AppCompatActivity() {

    private var editingExisting = false
    private var fighter: Fighter? = null
    private var fighterId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_fighter)
        fighterId = intent.getIntExtra("fighter", -1)
        if(fighterId != -1)
            initFields()
        supportActionBar?.title = if(editingExisting) getString(R.string.edit_fighter) else getString(R.string.add_fighter)
    }

    private fun initFields() {
        editingExisting = true
        val ref = App.Globals.db.reference.child("fighters").child(fighterId.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fighter = dataSnapshot.getValue(Fighter::class.java)
                editFirstName.setText(fighter!!.firstName)
                editLastName.setText(fighter!!.lastName)
                editUrl.setText(if(fighter!!.photoURL == null) "" else fighter!!.photoURL)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fighter.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.save_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.save -> {
                if(editingExisting)
                    saveChanges()
                else
                    saveFighterNew()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveFighterNew(){
        val firstName = editFirstName.text.toString()
        val lastName = editLastName.text.toString()
        val url: String = editUrl.text.toString()
        var isImage = false
        try {
            val mimeType = URLConnection.guessContentTypeFromName(url)
            isImage = mimeType != null && mimeType.startsWith("image")
        } catch (e: Exception) { }

        val fighter = Fighter(App.Globals.TourneyID, firstName, lastName, if(isImage) url else "")

        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighter.id.toString())
                .setValue(fighter)

        startActivity(Intent(this, ViewRosterActivity::class.java))
        finish()
    }

    private fun saveChanges(){
        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighterId.toString())
                .setValue(fighter)

        startActivity(Intent(this, ViewRosterActivity::class.java))
        finish()
    }

    private fun imageUrlParser(url: String): String? {
        var isImage = false
        try {
            val mimeType = URLConnection.guessContentTypeFromName(url)
            isImage = mimeType != null && mimeType.startsWith("image")
        } catch (e: Exception) { }
        if(isImage) return url
        return null
    }

}
