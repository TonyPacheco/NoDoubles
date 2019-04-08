package com.nodoubles.app.Models

class Fight () {

    companion object {
        const val STATUS_AWAITING = 1
        const val STATUS_FIGHTING = 0
        const val STATUS_FINISHED = 2
    }
    
     var id: Int = -1
     var tourney: Int = 0
     var fighter1: Fighter
     var fighter2: Fighter
     var matchType: String = ""
     var day: String = ""
     var time: String = ""
     var fighter1Pts: Int = 0
     var fighter2Pts: Int = 0
     var status: Int = 1

    init{
        fighter1 = Fighter()
        fighter2 = Fighter()
    }

    constructor(tourney:Int, fighter1:Fighter, fighter2:Fighter, matchType:String, day:String, time:String ) : this() {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
        this.tourney = tourney
        this.fighter1 = fighter1
        this.fighter2 = fighter2
        this.matchType = matchType
        this.day = day
        this.time = time
        status = STATUS_AWAITING
    }

    fun startFight() {
        status = STATUS_FIGHTING
    }

    fun resolveFight() {
        status = STATUS_FINISHED
    }

}