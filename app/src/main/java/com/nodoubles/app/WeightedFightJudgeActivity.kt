package com.nodoubles.app
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import com.nodoubles.app.Models.ScoreScheme
import kotlinx.android.synthetic.main.activity_weighted_fight_judge.*

class WeightedFightJudgeActivity : AppCompatActivity() {

    private var fight: Fight? = null
    private var fighter1: Fighter = Fighter(0)
    private var fighter2: Fighter = Fighter(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weighted_fight_judge)
        supportActionBar?.title = "FIGHT"
        getFightFromFb(intent.getIntExtra("fight", 0))
        getSchemeFromFb()

        pts1!!.text = "0"
        pts2!!.text = "0"

        plus1!!.setOnClickListener{
            addScore(1,1)
            update()
        }
        plus2!!.setOnClickListener{
            addScore(2, 1)
            update()
        }
        minus1!!.setOnClickListener{
            addScore(1,-1)
            update()
        }
        minus2!!.setOnClickListener{
            addScore(2, -1)
            update()
        }
        closedDbl1!!.setOnClickListener{
            addScore(1,2)
            addScore(2,1)
            update()
        }
        closedDbl2!!.setOnClickListener {
            addScore(1, 1)
            addScore(2, 2)
            update()
        }
        doubleHit!!.setOnClickListener{
            addScore(1,-1)
            addScore(2,-1)
            update()
        }
        btn_end_fight!!.setOnClickListener{
            finaliseFight()
        }
    }

    private fun initButtons(scheme: ScoreScheme?){
        if(scheme != null){
            hand1!!.setOnClickListener{
                addScore(1,scheme.hand)
                update()
            }
            hand2!!.setOnClickListener{
                addScore(2,scheme.hand)
                update()
            }
            arm1!!.setOnClickListener{
                addScore(1,scheme.arms)
                update()
            }
            arm2!!.setOnClickListener{
                addScore(2,scheme.arms)
                update()
            }
            body1!!.setOnClickListener{
                addScore(1,scheme.body)
                update()
            }
            body2!!.setOnClickListener{
                addScore(2,scheme.body)
                update()
            }
            head1!!.setOnClickListener{
                addScore(1,scheme.head)
                update()
            }
            head2!!.setOnClickListener{
                addScore(2,scheme.head)
                update()
            }
            leg1!!.setOnClickListener{
                addScore(1,scheme.legs)
                update()
            }
            leg2!!.setOnClickListener{
                addScore(2,scheme.legs)
                update()
            }
        }
        else
        {
            hand1!!.setOnClickListener{
                addScore(1,1)
                update()
            }
            hand2!!.setOnClickListener{
                addScore(2,1)
                update()
            }
            arm1!!.setOnClickListener{
                addScore(1,2)
                update()
            }
            arm2!!.setOnClickListener{
                addScore(2,2)
                update()
            }
            body1!!.setOnClickListener{
                addScore(1,3)
                update()
            }
            body2!!.setOnClickListener{
                addScore(2,3)
                update()
            }
            head1!!.setOnClickListener{
                addScore(1,4)
                update()
            }
            head2!!.setOnClickListener{
                addScore(2,4)
                update()
            }
            leg1!!.setOnClickListener{
                addScore(1,4)
                update()
            }
            leg2!!.setOnClickListener{
                addScore(2,4)
                update()
            }
        }
    }

    private fun addScore(fighter: Int, amt: Int){
        when(fighter){
            1 -> {
                fighter1.addToCurrentScore(amt)
                fight!!.fighter1Pts += amt
            }
            2-> {
                fighter2.addToCurrentScore(amt)
                fight!!.fighter2Pts += amt
            }
        }
    }

    private fun pingFight() {
        App.Globals.db.reference.child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fight?.id.toString())
                .setValue(fight)
    }

    private fun pingFighters(){
        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighter1.id.toString())
                .setValue(fighter1)

        App.Globals.db.reference.child("fighters")
                .child(App.Globals.TourneyID.toString())
                .child(fighter2.id.toString())
                .setValue(fighter2)
    }

    private fun update(){
        pingFighters()
        pingFight()
        pts1!!.text = fighter1.currentScore.toString()
        pts2!!.text = fighter2.currentScore.toString()
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
        update()
        finish()
    }

    private fun getFightFromFb(fightId: Int){
        val ref = App.Globals.db.reference.child("fights")
                .child(App.Globals.TourneyID.toString())
                .child(fightId.toString())

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                fight = dataSnapshot.getValue(Fight::class.java)
                fight!!.startFight()
                fighter1 = fight!!.fighter1
                fighter2 = fight!!.fighter2
                name1!!.text = fighter1.getFullName()
                name2!!.text = fighter2.getFullName()
                update()
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fight.",
                        Toast.LENGTH_SHORT).show()
            }
        }
        ref.addListenerForSingleValueEvent(listener)
    }

    private fun getSchemeFromFb(){
        val ref = App.Globals.db.reference.child("schemes")
                .child(App.Globals.tourney!!.scoreSchemeId)

        val listener = object : ValueEventListener {
            override fun onDataChange(scheme: DataSnapshot) {
                val s = scheme.getValue(ScoreScheme::class.java)
                initButtons(s)
            }
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fight scheme.",
                        Toast.LENGTH_SHORT).show()
            }
        }

        ref.addListenerForSingleValueEvent(listener)

    }

}
