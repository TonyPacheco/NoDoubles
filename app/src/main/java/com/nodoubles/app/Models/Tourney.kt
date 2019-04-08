package com.nodoubles.app.Models

class Tourney() {

    companion object {
        const val SCORE_TYPE_STANDARD_WEIGHTING = 0
        const val SCORE_TYPE_REGION_BASED_SCORE = 1
        const val STANDARD_SCHEME_ID = "4_3_2_1_3"
    }

    var id: Int = 0
    var name: String = ""
    var photoURL: String? = null
    val organizers: ArrayList<String> = ArrayList()
    var admin: String = ""

    var scoreType: Int = SCORE_TYPE_STANDARD_WEIGHTING
    var scoreSchemeId: String = STANDARD_SCHEME_ID

    init {
        id = System.currentTimeMillis().toInt() % Integer.MAX_VALUE
    }

    constructor(name: String, creator: String, photoURL: String, scoreType: Int) : this() {
        this.name = name
        this.organizers.add(creator)
        admin = creator
        this.photoURL = photoURL
        this.scoreType = scoreType
    }

    constructor(name: String, creator: String, photoURL: String, scoreType: Int, scheme: String) :
            this(name, creator, photoURL, scoreType){
        this.scoreSchemeId = scheme
    }


}