package com.nodoubles.app
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Adapters.TournamentListAdapter
import com.nodoubles.app.App.Globals.auth
import com.nodoubles.app.App.Globals.db
import com.nodoubles.app.App.Globals.isAdmin
import com.nodoubles.app.App.Globals.isLoggedIn
import com.nodoubles.app.Models.Tourney
import kotlinx.android.synthetic.main.activity_find_tourney.*
import com.nodoubles.app.R

class FindTourneyActivity : AppCompatActivity() {

    private var ref = App.Globals.db.reference.child("tournaments")
    private var tournaments: ArrayList<Tourney> = ArrayList()

    override fun onStart() {
        super.onStart()
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                tournaments.clear()
                for (postSnapshot in dataSnapshot.children) {
                    val t = postSnapshot.getValue(Tourney::class.java)!!
                    if(!isLoggedIn() || t.organizers.contains(App.Globals.auth.currentUser!!.uid))
                        tournaments.add(t)
                }
                rec_list.adapter = TournamentListAdapter(App.Globals.ctx(), tournaments)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load tournaments.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(listener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_tourney)
        supportActionBar?.title = Html.fromHtml("<font color=\"#00202b\">" + getString(R.string.find_tourney) + "</font>")
        ref = db.reference.child("tournaments")
        rec_list.setHasFixedSize(true)

        rec_list.layoutManager = LinearLayoutManager(this)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.new_tourney -> {
                startActivity(Intent(this, OrganizeTournament::class.java))
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(isLoggedIn())
            menuInflater.inflate(R.menu.tourney_admin_menu, menu)
        return true
    }

    override fun onBackPressed() {
        auth.signOut()
        startActivity(Intent(this, SplashActivity::class.java))
        finish()
    }

}
