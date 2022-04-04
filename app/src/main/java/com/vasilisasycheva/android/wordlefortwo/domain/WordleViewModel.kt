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

    private var _gbState = MutableLiveData(GuessBoardState()) // state for square and rows moving
    val gbState: LiveData<GuessBoardState> = _gbState
    private val currentGbState
        get() = _gbState.value ?: GuessBoardState()

    private var _roundState = MutableLiveData(RoundState()) //state for win, lose, wordcheck, result display
    val roundState: LiveData<RoundState> = _roundState
    private val currentRoundState
        get() = _roundState.value ?: RoundState()

    private var _sState = MutableLiveData(ScoreState())
    val sState: LiveData<ScoreState> = _sState
    private val currentSState
        get() = _sState.value ?: ScoreState()

    fun setWord(s: String) {
        updateRoundState { it.copy(wordToGuess = s.uppercase()) }
    }

    fun squareFocusForward() {
        if (currentGbState.squareInFocus < 4) updateGbState {
            it.copy(
                squareInFocus = currentGbState.squareInFocus + 1
            )
        }
    }

    fun squareFocusBackwards() {
        updateGbState {
            it.copy(
                squareInFocus =
                if (currentGbState.squareInFocus > 1) currentGbState.squareInFocus - 1 else 0
            )
        }
    }

    private fun nextRow() {
        if (currentGbState.currentRow < 5) {
            updateGbState {
                it.copy(
                    currentRow = currentGbState.currentRow + 1,
                    squareInFocus = 0
                )
            }
            updateRoundState { it.copy(checkResult = emptyMap()) }
        }
    }

    fun checkWord(result: String): Boolean {
        /*Checks if the word is in the prepared file with only 5-letter nouns*/
        var isOk = false
        getApplication<Application>()
            .assets
            .open(FILE_NAME)
            .reader()
            .forEachLine {
                if (result == it.uppercase()) isOk = true
            }
        updateRoundState { it.copy(wordCheck = isOk) }
        return isOk
    }

    fun checkResult(result: String) {
        checkLetters(result)
        checkWin()
        checkLoss()
        nextRow()
    }

    private fun checkLetters(result: String) {
        /* Creates a map result with postions and status of all the keys,
        * Indexes matter in Char match case as it could be more than one same letters in the word
        * Checked letter are replaced so when word contain more that one same char
        * it will be displayed correctly in the check results*/
        val posMatch: MutableMap<Int, Char> = mutableMapOf()
        val charMatch: MutableMap<Int, Char> = mutableMapOf()
        val miss: MutableMap<Int, Char> = mutableMapOf()
        var word = currentRoundState.wordToGuess

        result.forEachIndexed { ind, char ->
            when (char) {
                word[ind] -> {
                    posMatch[ind] = char
                    word = word.replaceFirst(char, '1')
                }
                in word -> {
                    charMatch[ind] = char
                    word = word.replaceFirst(char, '1')
                }
                !in word -> {
                    miss[ind] = char
                    word = word.replaceFirst(char, '1')
                }
            }
        }

        updateRoundState {
            it.copy(checkResult = mapOf(
                GuessState.Miss to miss,
                GuessState.Positionmatch to posMatch,
                GuessState.Charmatch to charMatch
            ))
        }
    }

    private fun checkWin() {
        val posMatch = currentRoundState.checkResult[GuessState.Positionmatch]!!
        if (posMatch.size == 5) {
            updateRoundState { it.copy(isWin = true) }
            increaseScore()
            changePlayer()
        }
    }

    private fun checkLoss() {
        if (!currentRoundState.isWin && currentGbState.currentRow == 5) {
            updateRoundState { it.copy(isLost = true) }
        }
    }

    private fun changePlayer() {
        when {
            currentSState.player1.isGuessing -> {
                updateSState {
                    it.copy(
                        player1 = currentSState.player1.copy(isGuessing = false),
                        player2 = currentSState.player2.copy(isGuessing = true)
                    )
                }
            }
            currentSState.player2.isGuessing -> {
                updateSState {
                    it.copy(
                        player1 = currentSState.player1.copy(isGuessing = true),
                        player2 = currentSState.player2.copy(isGuessing = false)
                    )
                }
            }
        }
    }

    private fun increaseScore() {
        when {
            currentSState.player1.isGuessing -> {
                updateSState { it.copy(player1 = currentSState.player1.increaseScore()) }
            }
            currentSState.player2.isGuessing -> {
                updateSState { it.copy(player2 = currentSState.player2.increaseScore()) }
            }

        }
    }

    private fun resetRound() {
        updateRoundState { RoundState() }
    }

    private fun resetGuessBoard() {
        updateGbState {
            it.copy(
                currentRow = 0,
                squareInFocus = 0
            )
        }
    }

    fun endRound() {
        resetRound()
        resetGuessBoard()
    }

    private fun updateRoundState(update: (curState: RoundState) -> RoundState) {
        val updatedState = update(currentRoundState)
        _roundState.value = updatedState
    }

    private fun updateGbState(update: (curState: GuessBoardState) -> GuessBoardState) {
        val updatedState = update(currentGbState)
        _gbState.value = updatedState
    }

    private fun updateSState(update: (curState: ScoreState) -> ScoreState) {
        val updatedState = update(currentSState)
        _sState.value = updatedState
    }
}

data class RoundState(
    val wordToGuess: String = "",
    val wordCheck: Boolean = false,
    val isWin: Boolean = false,
    val isLost: Boolean = false,
    val checkResult: Map<GuessState, Map<Int, Char>> = emptyMap()
)

data class GuessBoardState(
    var currentRow: Int = 0,
    val squareInFocus: Int = 0,
)

data class ScoreState(
    val player1: Player = Player(1, R.color.char_match).apply { isGuessing = true },
    val player2: Player = Player(2, R.color.position_match),
)