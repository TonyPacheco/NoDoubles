package com.nodoubles.app
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.view.Menu
import android.view.MenuItem
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_organize_fight.*
import java.util.*
import android.widget.TimePicker
import android.app.TimePickerDialog
import android.content.Intent
import android.widget.DatePicker
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Adapters.RosterAdapter
import com.nodoubles.app.Models.Tourney
import kotlinx.android.synthetic.main.activity_organize_tournament.*
import kotlinx.android.synthetic.main.activity_view_roster.*
import java.net.URLConnection
import java.text.SimpleDateFormat


class OrganizeFightActivity : AppCompatActivity(),
                              DatePickerDialog.OnDateSetListener,
                              TimePickerDialog.OnTimeSetListener {

    private var fighters = ArrayList<Fighter>()

    private var date = ""
    private var time = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_organize_fight)
        supportActionBar?.title = getString(R.string.create_fight)
        getFightersFromDb()
        fightDay.setOnClickListener{
            val frag = DateFragment()
            frag.show(fragmentManager, "datePicker")
        }
        fightDay.inputType = InputType.TYPE_NULL
        fightTime.setOnClickListener{
            val frag = TimeFragment()
            frag.show(fragmentManager, "timePicker")
        }
        fightTime.inputType = InputType.TYPE_NULL
    }


    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        date = SimpleDateFormat("MMM", Locale.CANADA).format(p2) + " " + p3
        fightDay.setText(date)
    }

    override fun onTimeSet(p0: TimePicker?, p1: Int, p2: Int) {
        time =  String.format("%d:%d", p1, p2)
        fightTime.setText(time)
    }

    private fun getFightersFromDb () {
        val ref = App.Globals.db.reference.child("fighters").child(App.Globals.TourneyID.toString())
        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (postSnapshot in dataSnapshot.children) {
                    fighters.add(postSnapshot.getValue(Fighter::class.java)!!)
                }
                val adapter = ArrayAdapter<String>(
                        App.Globals.ctx(), android.R.layout.simple_spinner_item, getNamesFromFighters())
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                fighter1.adapter = adapter
                fighter2.adapter = adapter
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load tournaments.",
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
                saveFight()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getNamesFromFighters() : ArrayList<String> {
        val list = ArrayList<String>()
        for(fighter in fighters)
            list.add(fighter.getFullName())
        return list
    }

    private fun saveFight(){
        val selFighter1 = fighters[fighter1.selectedItemPosition]
        val selFighter2 = fighters[fighter2.selectedItemPosition]
        if(selFighter1 == selFighter2){
            Toast.makeText(this,"Fighters can't fight themselves!", Toast.LENGTH_LONG).show()
            return
        }
        val type = fightType.text.toString()
        val day = fightDay.text.toString()
        val time = fightTime.text.toString()
        val fight = Fight(App.Globals.TourneyID,
                selFighter1, selFighter2,
                type, day, time)

        App.Globals.db.reference.child("fights").child(App.Globals.TourneyID.toString())
                .child(fight.id.toString())
                .setValue(fight)

        startActivity(Intent(this, ViewFightsActivity::class.java))
        finish()
    }


    internal class DateFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val listener = activity as DatePickerDialog.OnDateSetListener

            return DatePickerDialog(activity, listener, year, month, day)
        }

    }

    internal class TimeFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val listener = activity as TimePickerDialog.OnTimeSetListener

            return TimePickerDialog(activity, listener, hour, minute, false)
        }

    }
}
