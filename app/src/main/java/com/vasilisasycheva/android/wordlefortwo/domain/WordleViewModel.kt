package com.vasilisasycheva.android.wordlefortwo.domain

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vasilisasycheva.android.wordlefortwo.R
import com.vasilisasycheva.android.wordlefortwo.model.Player
import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.GuessState

const val FILE_NAME = "shortwordslist.txt"
const val DEBUG_TAG = "wordle_debug"
class WordleViewModel(application: Application) : AndroidViewModel(application) {

    private var wordToGuess = MutableLiveData("")
    val word:LiveData<String> = wordToGuess
    val currentRow: MutableLiveData<Int> = MutableLiveData(0)
    val squareInFocus: MutableLiveData<Int> = MutableLiveData(0)
    val wordCheck: MutableLiveData<Boolean> = MutableLiveData()
    val isWin: MutableLiveData<Boolean> = MutableLiveData(false)
    val isLost: MutableLiveData<Boolean> = MutableLiveData()
    val player1: MutableLiveData<Player> = MutableLiveData(Player(1, R.color.char_match))
    val player2: MutableLiveData<Player> = MutableLiveData(Player(2, R.color.position_match))
    val checkResult = MutableLiveData<Map<GuessState, MutableMap<Int, Char>>>()

    init {
        player2.value?.isGuessing = true
    }

    fun setWord(s: String) {
        wordToGuess.value = s.uppercase()
    }

    fun squareFocusForward() {
        squareInFocus.value?.let { index ->
            if (index < 4) {
                squareInFocus.value = index + 1
            }
        }
    }

    fun squareFocusBackwards() {
        squareInFocus.value?.let { index ->
            if (index > 0) {
                squareInFocus.value = index - 1
            }
        }
    }

    private fun nextRow() {
        currentRow.value?.let { index ->
            if (index < 5) {
                currentRow.value = index + 1
            }
        }
        squareInFocus.value = 0
    }

    fun checkWord(result: String): Boolean {
        var isOk = false
        getApplication<Application>()
            .assets
            .open(FILE_NAME)
            .reader()
            .forEachLine {
                if (result == it.uppercase()) isOk = true
            }
        wordCheck.value = isOk
        Log.d(DEBUG_TAG, "word is ok: $isOk")
        return isOk
    }

    fun checkResult(result: String) {
        val posMatch: MutableMap<Int, Char> = mutableMapOf()
        val charMatch: MutableMap<Int, Char> = mutableMapOf()
        val miss: MutableMap<Int, Char> = mutableMapOf()

        result.forEachIndexed { ind, char ->
            when (char) {
                wordToGuess.value!![ind] -> {
                    posMatch[ind] = char
                }
                in wordToGuess.value!! -> {
                    charMatch[ind] = char
                }
                !in wordToGuess.value!! -> {
                    miss[ind] = char
                }
            }
        }
        checkResult.value = mapOf(
            GuessState.Miss to miss,
            GuessState.Positionmatch to posMatch,
            GuessState.Charmatch to charMatch
        )
        checkWin(posMatch)
        checkLoss()
        nextRow()
    }

    private fun checkWin(posMatch: MutableMap<Int, Char>) {
        if (posMatch.size == 5) {
            isWin.value = true
            increaseScore()
            changePlayer()
        }
    }

    private fun checkLoss() {
        if (!isWin.value!! && currentRow.value == 5) {
            isLost.value = true
        }
    }

    private fun changePlayer() {
        when {
            player1.value!!.isGuessing -> {
                player1.value = player1.value!!.copy(isGuessing = false)
                player2.value = player2.value!!.copy(isGuessing = true)
            }
            player2.value!!.isGuessing -> {
                player2.value = player2.value!!.copy(isGuessing = false)
                player1.value = player1.value!!.copy(isGuessing = true)
            }
        }
    }

    private fun increaseScore() {
        when {
            player1.value!!.isGuessing -> {
                player1.value?.score?.let { score ->
                    player1.value = player1.value!!.copy(score = score + 1)
                }
            }
            player2.value!!.isGuessing -> {
                player2.value!!.score?.let { score ->
                    player2.value = player2.value!!.copy(score = score + 1)
                }
            }
        }
    }

    fun endRound() {
        wordToGuess.value = ""
        isWin.value = false
        currentRow.value = 0
        squareInFocus.value = 0
    }

//    private fun updatePlayer(player: MutableLiveData<Player>, update: (currentState: Player) -> Player) {
//        val updateState: Player = player.value?.let { update(it) }
//        player.value
//    }
}