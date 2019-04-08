package com.nodoubles.app.Models

class Fighter() {
    var id: Int = -1
    var tournament: Int = -1
    var firstName: String = ""
    var lastName: String = ""
    var bio: String? = null /* IGNORE */
    var photoURL: String? = null
    var currentScore: Int = 0
    var tourneyScore: Int = 0
    var wins: Int = 0
    var losses: Int = 0

    public var headHitsGiven = 0
    public var headHitsRecvd = 0
    public var bodyHitsGiven = 0
    public var bodyHitsRecvd = 0
    public var armsHitsGiven = 0
    public var armsHitsRecvd = 0
    public var handHitsGiven = 0
    public var handHitsRecvd = 0
    public var legsHitsGiven = 0
    public var legsHitsRecvd = 0
    public var openDbExchngs = 0
    public var closedDbGiven = 0
    public var closedDbRecvd = 0

    init {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
    }

    constructor (tournament: Int) : this() {
        this.tournament = tournament
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
    }

    constructor (tournament: Int, idSpoofer: Int) : this(tournament) {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE - idSpoofer
    }

    constructor (tournament: Int, f: String, l:String, url:String) : this(tournament){
        firstName = f
        lastName = l
        photoURL = url
    }

    internal fun resolveFight(won: Boolean) {
        if (won)
            wins++
        else
            losses++
        tourneyScore += currentScore
        currentScore = 0
    }

    fun addToCurrentScore(score: Int) {
        this.currentScore += score
    }

    fun removeWin() {
        wins--
    }

    fun removeLoss() {
        losses--
    }

    fun getFullName(): String {
        return String.format("%s %s", firstName, lastName)
    }

}

