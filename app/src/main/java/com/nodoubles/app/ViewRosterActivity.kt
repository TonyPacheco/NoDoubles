package com.nodoubles.app
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.nodoubles.app.Adapters.RosterAdapter
import com.nodoubles.app.Adapters.TournamentListAdapter
import com.nodoubles.app.Models.Fighter
import com.nodoubles.app.Models.Tourney
import kotlinx.android.synthetic.main.activity_edit_fighter.*
import kotlinx.android.synthetic.main.activity_find_tourney.*
import kotlinx.android.synthetic.main.activity_view_roster.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URLConnection
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class ViewRosterActivity : AppCompatActivity() {

    private val fighters = ArrayList<Fighter>()
    private val fighterImports = ArrayList<Fighter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_roster)
        supportActionBar?.title = Html.fromHtml("<font color=\"#00202b\">" + getString(R.string.tourney_roster) + "</font>")
        nav_bar_roster.setOnNavigationItemSelectedListener{
            when (it.itemId){
                R.id.view_fights -> {
                    val intent = Intent(this,ViewFightsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                    startActivity(intent)
                    finish()/* TODO:NOT THIS, reloads db on each press, but prevents app from re-entering here */
                }
            }
            true
        }

        getFightersFromFb(this)

        rec_list_roster.setHasFixedSize(true)
        rec_list_roster.adapter = RosterAdapter(this, fighters)
        rec_list_roster.layoutManager = LinearLayoutManager(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if(App.Globals.isAdmin)
            menuInflater.inflate(R.menu.roster_admin_menu, menu)
        else
            menuInflater.inflate(R.menu.viewer_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId
        when(id){
            R.id.add_fighter -> {
                startActivity(Intent(this, EditFighterActivity::class.java))
                return true
            }
            R.id.import_fighters -> {
                showFileChooser()
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


    private fun getFightersFromFb (ctx: Context) {
        val ref = App.Globals.db.reference.child("fighters").child(App.Globals.TourneyID.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fighters.clear()
                for (postSnapshot in dataSnapshot.children) {
                    fighters.add(postSnapshot.getValue(Fighter::class.java)!!)
                }
                rec_list_roster.adapter = RosterAdapter(ctx, fighters)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load tournaments.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addValueEventListener(listener)
    }

    private fun showFileChooser(){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "text/*"
        startActivityForResult(intent, App.Globals.CSV_REQUEST_INT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == App.Globals.CSV_REQUEST_INT && resultCode == Activity.RESULT_OK){
            if(data != null){
                when(readCSV(data.data)){
                    App.Globals.READ_SUCCESSFUL -> saveImportsToDb()
                    App.Globals.CSV_PARSE_ERROR -> Toast.makeText(this, getString(R.string.error_parsing_csv), Toast.LENGTH_LONG).show()
                    App.Globals.HEADER_MISMATCH -> Toast.makeText(this, getString(R.string.incorrect_header) , Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun readCSV (uri: Uri) : Int {
        val ins = contentResolver.openInputStream(uri)
        val reader = BufferedReader(InputStreamReader(ins, Charset.forName("UTF-8")))
        var line: String? = null
        var idSpoofer = 999
        try {
            line = reader.readLine()
            if (!headerMatchesFormat(line))
                return App.Globals.HEADER_MISMATCH
            line = reader.readLine()
            while (line != null) {
                idSpoofer += 123
                val tokens = line.split(",")
                val fighter = Fighter(App.Globals.TourneyID, idSpoofer)
                fighter.firstName = tokens[0]
                fighter.lastName = tokens[1]
                fighter.photoURL = if (tokens.size >= 3 && tokens[2].isNotEmpty()) tokens[2] else null
                fighterImports.add(fighter)
                line = reader.readLine()
            }
        } catch (e: Exception) {
            return App.Globals.CSV_PARSE_ERROR
        }
        return App.Globals.READ_SUCCESSFUL
    }

    private fun saveImportsToDb(){
        val ref = App.Globals.db.reference.child("fighters").child(App.Globals.TourneyID.toString())
        for(f in fighterImports)
            ref.child(f.id.toString()).setValue(f)
    }

    private fun headerMatchesFormat(line: String) : Boolean {
        return line == "First,Last,ImageURL"
    }

    override fun onBackPressed() {
        App.Globals.TourneyID = -1
        startActivity(Intent(this, FindTourneyActivity::class.java))
        finish()
    }

}
