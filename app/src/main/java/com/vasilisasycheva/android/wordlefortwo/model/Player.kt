package com.vasilisasycheva.android.wordlefortwo.model

data class Player(val id: Int,
                  var color: Int,
                  var isGuessing: Boolean = false,
                  var score: Int = 0) {
    fun increaseScore(): Player {
        return this.copy(score = score + 1)
    }
}