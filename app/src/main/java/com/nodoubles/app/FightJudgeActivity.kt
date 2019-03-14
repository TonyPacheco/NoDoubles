package com.nodoubles.app
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import kotlinx.android.synthetic.main.activity_edit_fighter.*

import kotlinx.android.synthetic.main.activity_fight_judge.*

class FightJudgeActivity : AppCompatActivity() {

    private var fight: Fight? = null
    private var fighter1: Fighter = Fighter(0)
    private var fighter2: Fighter = Fighter(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fight_judge)
        supportActionBar?.title = "FIGHT"
        getFightFromFb(intent.getIntExtra("fight", 0))

        pts1!!.text = "0"
        pts2!!.text = "0"

        minus1!!.setOnClickListener{
            if(fighter1.currentScore > 0)
                fighter1.addToCurrentScore(-1)
            pingDb()
        }
        minus2!!.setOnClickListener{
            if(fighter2.currentScore > 0)
                fighter2.addToCurrentScore(-1)
            pingDb()
        }
        closedDbl1!!.setOnClickListener{
            fighter1.addToCurrentScore(2)
            fighter2.addToCurrentScore(1)

            pingDb()
        }
        closedDbl2!!.setOnClickListener{
            fighter1.addToCurrentScore(1)
            fighter2.addToCurrentScore(2)
            pingDb()
        }
        plus1!!.setOnClickListener{
            fighter1.addToCurrentScore(1)
            pingDb()
        }
        plus2!!.setOnClickListener{
            fighter2.addToCurrentScore(1)
            pingDb()
        }
        two1!!.setOnClickListener{
            fighter1.addToCurrentScore(2)
            pingDb()
        }
        two2!!.setOnClickListener{
            fighter2.addToCurrentScore(2)
            pingDb()
        }
        three1!!.setOnClickListener{
            fighter1.addToCurrentScore(3)
            pingDb()
        }
        three2!!.setOnClickListener{
            fighter2.addToCurrentScore(3)
            pingDb()
        }
        four1!!.setOnClickListener{
            fighter1.addToCurrentScore(4)
            pingDb()
        }
        four2!!.setOnClickListener{
            fighter2.addToCurrentScore(4)
            pingDb()
        }
        doubleHit!!.setOnClickListener{
            fighter1.addToCurrentScore(-1)
            fighter2.addToCurrentScore(-1)
            pingDb()
        }
        btn_end_fight!!.setOnClickListener{
            finaliseFight()
        }
    }

    private fun initPing() {
        App.Globals.db.reference.child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fight?.id.toString())
                .setValue(fight)
    }

    private fun pingDb(){
        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighter1.id.toString())
                .setValue(fighter1)
        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighter2.id.toString())
                .setValue(fighter2)
        pts1!!.text = fighter1.currentScore.toString()
        pts2!!.text = fighter2.currentScore.toString()
    }

    private fun finalPing(){
        pingDb()
        App.Globals.db.reference.child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fight?.id.toString())
                .setValue(fight)
    }

    private fun manageFightEnd(){
        var winner = 0 // In the event of a tie, both fighters lose
        if(fighter1.currentScore > fighter2.currentScore) winner = 1
        if(fighter2.currentScore > fighter1.currentScore) winner = 2
        fight?.resolveFight()
        fighter1.resolveFight(winner == 1)
        fighter2.resolveFight(winner == 2)
    }

    override fun onBackPressed() {
        finaliseFight()
    }

    private fun finaliseFight(){
        manageFightEnd()
        finalPing()
        finish()
    }

    fun getFightFromFb(fightId: Int){
        val ref = App.Globals.db.reference.child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fightId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fight = dataSnapshot.getValue(Fight::class.java)
                fight!!.startFight()
                initPing()
                fighter1 = fight!!.fighter1
                fighter2 = fight!!.fighter2
                name1!!.text = fighter1.getFullName()
                name2!!.text = fighter2.getFullName()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fight.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }
}
