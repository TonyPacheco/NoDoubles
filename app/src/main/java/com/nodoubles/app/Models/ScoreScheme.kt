package com.nodoubles.app.Models

data class ScoreScheme(var head: Int = 4,
                       var body: Int = 3,
                       var arms: Int = 2,
                       var hand: Int = 1,
                       var legs: Int = 3)
{
    val id = "" + head + "_" + body + "_" + arms + "_" + hand + "_" + legs
}