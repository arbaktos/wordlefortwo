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

    private var _gbState = MutableLiveData(GuessBoardState())
    val gbState: LiveData<GuessBoardState> = _gbState
    private val currentGbState
        get() = _gbState.value ?: GuessBoardState()

    private var _roundState = MutableLiveData(RoundState())
    val roundState: LiveData<RoundState> = _roundState
    private val currentRoundState
        get() = _roundState.value ?: RoundState()

    private var _sState = MutableLiveData(ScoreState())
    val sState: LiveData<ScoreState> = _sState
    private val currentSState
        get() = _sState.value ?: ScoreState()

    //    val isWin: MutableLiveData<Boolean> = MutableLiveData(false)
//    val isLost: MutableLiveData<Boolean> = MutableLiveData()
//    val player1: MutableLiveData<Player> =
//        MutableLiveData(Player(1, R.color.char_match).apply { isGuessing = true })
//    val player2: MutableLiveData<Player> = MutableLiveData(Player(2, R.color.position_match))
//    val checkResult = MutableLiveData<Map<GuessState, MutableMap<Int, Char>>>()
    val wordCheck: MutableLiveData<Boolean> = MutableLiveData()


    private fun updateUiState(update: (curState: RoundState) -> RoundState) {
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


    fun setWord(s: String) {
        updateUiState { it.copy(wordToGuess = s.uppercase()) }
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
        updateGbState {
            it.copy(
                currentRow = currentGbState.currentRow + 1,
                squareInFocus = 0
            )
        }
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
//        updateState { it.copy(wordCheck = isOk) }
        wordCheck.value = isOk
        Log.d(DEBUG_TAG, "word is ok: $isOk")
        return isOk
    }

    fun checkResult(result: String) {
        checkLetters(result)
        checkWin(currentRoundState.checkResult[GuessState.Positionmatch]!!)
        checkLoss()
        nextRow()
    }

    private fun checkLetters(result: String) {
        val posMatch: MutableMap<Int, Char> = mutableMapOf()
        val charMatch: MutableMap<Int, Char> = mutableMapOf()
        val miss: MutableMap<Int, Char> = mutableMapOf()

        result.forEachIndexed { ind, char ->
            when (char) {
                currentRoundState.wordToGuess[ind] -> {
                    posMatch[ind] = char
                }
                in currentRoundState.wordToGuess -> {
                    charMatch[ind] = char
                }
                !in currentRoundState.wordToGuess -> {
                    miss[ind] = char
                }
            }
        }
        val resultMap = mapOf(
            GuessState.Miss to miss,
            GuessState.Positionmatch to posMatch,
            GuessState.Charmatch to charMatch
        )
        updateUiState {
            it.copy(checkResult = resultMap)
        }
    }

    private fun checkWin(posMatch: Map<Int, Char>) {
        if (posMatch.size == 5) {
            updateUiState { it.copy(isWin = true) }
            increaseScore()
            changePlayer()
        }
    }

    private fun checkLoss() {
        if (!currentRoundState.isWin && currentGbState.currentRow == 5) {
            updateUiState { it.copy(isLost = true) }
        }
    }

    private fun changePlayer() {
        when {
            currentSState.player1.isGuessing -> {
//                player1.value = player1.value!!.copy(isGuessing = false)
                updateSState {
                    it.copy(
                        player1 = currentSState.player1.copy(isGuessing = false),
                        player2 = currentSState.player2.copy(isGuessing = true)
                    )
                }
//                player2.value = player2.value!!.copy(isGuessing = true)
            }
            currentSState.player2.isGuessing -> {
                updateSState {
                    it.copy(
                        player1 = currentSState.player1.copy(isGuessing = true),
                        player2 = currentSState.player2.copy(isGuessing = false)
                    )
                }
            }
//            player2.value!!.isGuessing -> {
//                player2.value = player2.value!!.copy(isGuessing = false)
//                updateSState { it.copy(player1 = currentSState.player1.copy(isGuessing = true)) }
//            }
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
//            player2.value!!.isGuessing -> {
//                player2.value!!.score?.let { score ->
//                    player2.value = player2.value!!.copy(score = score + 1)
//                }
//            }
        }
    }

    private fun resetRound() {
        updateUiState { RoundState() }
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