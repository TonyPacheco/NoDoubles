package com.nodoubles.app
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

import com.nodoubles.app.Adapters.FightListAdapter
import com.nodoubles.app.Models.Fight

import kotlinx.android.synthetic.main.activity_view_fights.*

class ViewFightsActivity : AppCompatActivity() {

    val fights = ArrayList<Fight>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_fights)
        supportActionBar?.title = Html.fromHtml("<font color=\"#00202b\">" + getString(R.string.match_schedule) + "</font>")
        nav_bar_fights.setOnNavigationItemSelectedListener{
            when (it.itemId){
                R.id.view_roster -> {
                    val intent = Intent(this,ViewRosterActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    finish()//TODO:NOT THIS, reloads db on each press, but prevents app from re-entering here
                }
            }
            true
        }

        getFightsFromFb()

        rec_list_fights.setHasFixedSize(true)
        rec_list_fights.adapter = FightListAdapter(this, fights)
        rec_list_fights.layoutManager = LinearLayoutManager(this)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        Log.w("Is Admin: ", App.Globals.isAdmin.toString())
        if(App.Globals.isAdmin)
            menuInflater.inflate(R.menu.fights_admin_menu, menu)
        else
            menuInflater.inflate(R.menu.viewer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.add_fight -> {
                startActivity(Intent(this, OrganizeFightActivity::class.java))
                return true
            }
            R.id.log_out,
            R.id.exit_tourney -> {
                App.Globals.isAdmin = false
                App.Globals.auth.signOut()
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getFightsFromFb () {
        val ref = App.Globals.db.reference.child("fights").child(App.Globals.TourneyID.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fights.clear()
                for (postSnapshot in dataSnapshot.children) {
                    fights.add(postSnapshot.getValue(Fight::class.java)!!)
                }
                rec_list_fights.adapter = FightListAdapter(App.Globals.ctx(), fights)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fights.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(listener)
    }

    override fun onBackPressed() {
        App.Globals.TourneyID = -1
        startActivity(Intent(this, FindTourneyActivity::class.java))
        finish()
    }
}
