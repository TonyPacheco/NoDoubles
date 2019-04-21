package com.nodoubles.app
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.nodoubles.app.Models.Fight
import com.nodoubles.app.Models.Fighter
import com.nodoubles.app.Models.ScoreScheme
import kotlinx.android.synthetic.main.activity_weighted_fight_judge.*
import android.os.CountDownTimer



class WeightedFightJudgeActivity : AppCompatActivity() {

    private var fight: Fight? = null
    private var fighter1: Fighter = Fighter(0)
    private var fighter2: Fighter = Fighter(0)
    private lateinit var slider_minutes: NumberPicker
    private lateinit var slider_seconds: NumberPicker
    private var minutes = 1
    private var seconds = 30
    var timerTicking = false
    var timer: CountDownTimer? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weighted_fight_judge)
        supportActionBar?.title = "FIGHT"
        getFightFromFb(intent.getIntExtra("fight", 0))
        getSchemeFromFb()

        pts1!!.text = "0"
        pts2!!.text = "0"

        initTimer()

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
        btn_end_fight!!.setOnClickListener{
            finaliseFight()
        }
        btn_timer!!.setOnClickListener{
            toggleTimer()
        }
    }

    private fun initButtons(scheme: ScoreScheme){
        hand1!!.setOnClickListener{
            addScore(1,scheme.hand)
            fighter1.handHitsGiven++
            fighter2.handHitsRecvd++
            update()
        }
        hand2!!.setOnClickListener{
            addScore(2,scheme.hand)
            fighter2.handHitsGiven++
            fighter1.handHitsRecvd++
            update()
        }
        arm1!!.setOnClickListener{
            addScore(1,scheme.arms)
            fighter1.armsHitsGiven++
            fighter2.armsHitsRecvd++
            update()
        }
        arm2!!.setOnClickListener{
            addScore(2,scheme.arms)
            fighter2.armsHitsGiven++
            fighter1.armsHitsRecvd++
            update()
        }
        body1!!.setOnClickListener{
            addScore(1,scheme.body)
            fighter1.bodyHitsGiven++
            fighter2.bodyHitsRecvd++
            update()
        }
        body2!!.setOnClickListener{
            addScore(2,scheme.body)
            fighter2.bodyHitsGiven++
            fighter1.bodyHitsRecvd++
            update()
        }
        head1!!.setOnClickListener{
            addScore(1,scheme.head)
            fighter1.headHitsGiven++
            fighter2.headHitsRecvd++
            update()
        }
        head2!!.setOnClickListener{
            addScore(2,scheme.head)
            fighter2.headHitsGiven++
            fighter1.headHitsRecvd++
            update()
        }
        leg1!!.setOnClickListener{
            addScore(1,scheme.legs)
            fighter1.legsHitsGiven++
            fighter2.legsHitsRecvd++
            update()
        }
        leg2!!.setOnClickListener{
            addScore(2,scheme.legs)
            fighter2.legsHitsGiven++
            fighter1.legsHitsRecvd++
            update()
        }
        closedDbl1!!.setOnClickListener{
            addScore(1,scheme.clsg)
            addScore(2,scheme.clsr)
            fighter1.closedDbGiven++
            update()
        }
        closedDbl2!!.setOnClickListener {
            addScore(1, scheme.clsr)
            addScore(2, scheme.clsg)
            fighter2.closedDbGiven++
            update()
        }
        doubleHit!!.setOnClickListener{
            addScore(1,scheme.open)
            addScore(2,scheme.open)
            fighter1.openDbExchngs++
            fighter2.openDbExchngs++
            update()
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
                initButtons(s!!)
            }
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(baseContext, "Failed to load fight scheme.",
                        Toast.LENGTH_SHORT).show()
            }
        }

        ref.addListenerForSingleValueEvent(listener)

    }

    private fun initTimer(){
        slider_minutes = timer_minutes!!
        slider_seconds = timer_seconds!!
        val minuteOptions = Array(6) { "$it" }
        val secondOptions = Array(60){ "$it" }
        for(i in 0..9)
            secondOptions[i] = "0$i"

        slider_minutes.minValue = 0
        slider_minutes.maxValue = 5
        slider_minutes.wrapSelectorWheel = false
        slider_minutes.value = 1
        slider_minutes.displayedValues = minuteOptions
        slider_minutes.setOnScrollListener{ s_min: NumberPicker, i: Int ->

        }
        slider_minutes.setOnValueChangedListener{ _, _, new: Int ->
            minutes = new
            if(timerTicking)
                toggleTimer()
        }

        slider_seconds.minValue = 0
        slider_seconds.maxValue = 59
        slider_seconds.wrapSelectorWheel = false
        slider_seconds.value = 30
        slider_seconds.displayedValues = secondOptions
        slider_seconds.setOnScrollListener{ s_sec: NumberPicker, i: Int ->

        }
        slider_seconds.setOnValueChangedListener{ _, _, new: Int ->
            seconds = new
            if(timerTicking)
                toggleTimer()
        }
    }

    private fun toggleTimer(){
        timerTicking = !timerTicking
        btn_timer!!.text = if(timerTicking) "STOP" else "START"
        if(timerTicking){
            timer = object : CountDownTimer((minutes.toLong() * 60 * 1000) + (seconds * 1000), 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    --seconds
                    if(seconds < 0){
                        seconds = 59
                        if(minutes != 0)
                            --minutes
                    }
                    slider_minutes.value = minutes
                    slider_seconds.value = seconds
                }
                override fun onFinish() {
                    toggleTimer()
                }
            }.start()
        } else {
            timer!!.cancel()
        }
    }
}
