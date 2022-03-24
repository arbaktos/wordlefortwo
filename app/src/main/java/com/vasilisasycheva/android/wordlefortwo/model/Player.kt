package com.vasilisasycheva.android.wordlefortwo.model

data class Player(val id: Int,
                  var color: Int,
                  var isGuessing: Boolean = false,
                  var score: Int = 0)