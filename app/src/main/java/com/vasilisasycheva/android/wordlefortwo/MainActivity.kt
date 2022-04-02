package com.vasilisasycheva.android.wordlefortwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.domain.RoundState
import com.vasilisasycheva.android.wordlefortwo.domain.WordleViewModel
import com.vasilisasycheva.android.wordlefortwo.extensions.shake
import com.vasilisasycheva.android.wordlefortwo.ui.guessboard.GuessBoard
import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.*

class MainActivity : AppCompatActivity() {

    lateinit var currentRow: GuessBoard.RowOfSquares
    lateinit var squareInFocus: GuessBoard.Square
    lateinit var guessBoard: GuessBoard
    lateinit var keyBoard: Keyboard
    lateinit var score1: TextView
    lateinit var score2: TextView
    lateinit var btnDone: AppCompatButton
    lateinit var enterKey: Keyboard.EnterKey

    private var squareInd = 0
    private var resultKeys: MutableList<Keyboard.TextKey> = mutableListOf()
    private val vm: WordleViewModel by viewModels()
    private var squareList: MutableList<GuessBoard.Square> = mutableListOf()
    private var currentRoundState: RoundState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupScoresAndButton()
        setupKeyboard()
        setupGuessBoard()

        vm.gbState.observe(this) { gbState ->
            setupCurrentRow(gbState.currentRow)
            setCurrentSquare(gbState.squareInFocus)
            checkEnterKeyState()
        }

        vm.roundState.observe(this) { uiState ->
            currentRoundState = uiState

            isWordSet(uiState.wordToGuess.isNotEmpty())
            if (uiState.checkResult.isNotEmpty())
                displayCheckResults(uiState.checkResult) //? kind of ugly
            displayWin(uiState.isWin)
            createLoseDialog(uiState.isLost)
            if (!uiState.wordCheck && uiState.wordToGuess.isNotEmpty()) {
                currentRow.shake()
            }
        }

        vm.sState.observe(this) { scoreState ->
            score1.text = scoreState.player1.score.toString()
            score2.text = scoreState.player2.score.toString()
        }
    }

    private fun displayWin(win: Boolean) {
        if (win) createWinDialog()
    }

    private fun setCurrentSquare(sqInd: Int) {
        squareInd = sqInd
        squareInFocus = currentRow.getChildAt(squareInd) as GuessBoard.Square

    }

    private fun setupCurrentRow(rowInd: Int) {
        currentRow = guessBoard.getChildAt(rowInd) as GuessBoard.RowOfSquares
        val lastSquare = currentRow.getChildAt(4) as GuessBoard.Square
        lastSquare.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                checkEnterKeyState()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun checkEnterKeyState() {
        enterKey.isEnabled = checkIfRowIsFull() && squareInd == 4
    }

    private fun checkIfRowIsFull(): Boolean {
        var result = ""
        currentRow.children.forEach {
            it as GuessBoard.Square
            result += it.text.toString()
        }
        return result.length == 5
    }

    private fun clearField() {
        keyBoard.children.forEach { row ->
            row as Keyboard.Row
            row.children.forEach {
                it as Key
                if (it is Keyboard.TextKey) it.setKeyState()
            }
        }
        guessBoard.children.forEach { row ->
            row as GuessBoard.RowOfSquares
            row.children.forEach { square ->
                square as GuessBoard.Square
                square.setText("")
                square.setSquareStatus()
            }
        }
        vm.endRound()
    }

    private fun isWordSet(isSet: Boolean) {
        if (isSet) btnDone.visibility = View.INVISIBLE
        else btnDone.visibility = View.VISIBLE
        keyBoard.isEnabled(isSet)
    }

    private fun createLoseDialog(lost: Boolean) {
        if (lost) {
            AlertDialog.Builder(this)
                .setMessage(getString(R.string.loss_message) + " ${currentRoundState?.wordToGuess}")
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    clearField()
                }
                .show()
        }
    }

    private fun createWinDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.win_message))
            .setPositiveButton(getString(R.string.ok)) { _, _ ->
                clearField()
            }
            .show()
    }

    private fun createSetWordDialog() {
        val etWord = EditText(this)
//        val setWordRow = RowSeparate(this)
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.make_a_word_dialog))
            .setView(etWord)
            .setPositiveButton(getString(R.string.done)) { _, _ ->
                val text = etWord.text.toString()
                checkSetWord(text.uppercase())
            }.show()
    }

    private fun checkSetWord(word: String) {
        val isWordOk = vm.checkWord(word)
        if (!isWordOk && currentRoundState!!.wordToGuess.isEmpty()) {
            makeShortToast(this.getString(R.string.no_such_word_in_dict))
        } else {
            vm.setWord(word)
        }

    }

    private fun displayCheckResults(checkResult: Map<GuessState, Map<Int, Char>>) {
        resultKeys.forEach { textKey ->
            when (textKey.label.uppercase().first()) {
                in checkResult[GuessState.Positionmatch]!!.values -> textKey.setKeyState(
                    GuessState.Positionmatch
                )
                in checkResult[GuessState.Charmatch]!!.values -> textKey.setKeyState(
                    GuessState.Charmatch
                )
                in checkResult[GuessState.Miss]!!.values -> textKey.setKeyState(GuessState.Miss)
            }
        }

        checkResult[GuessState.Miss]?.let { missMap ->
            missMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(
                    GuessState.Miss
                )
            }
        }

        checkResult[GuessState.Charmatch]?.let { charMatchMap ->
            charMatchMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(
                    GuessState.Charmatch
                )
            }
        }
        checkResult[GuessState.Positionmatch]?.let { posMatchMap ->
            posMatchMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(
                    GuessState.Positionmatch
                )
            }
        }
    }

    inner class KeyboardClicksIntImpl : KeyboardClicksInt {
        override fun onTextClick(v: Keyboard.TextKey) {
            if (squareInFocus.text.isNullOrBlank()) {
                squareInFocus.setText(v.label.uppercase())
                resultKeys.add(v)
            } else {
                vm.squareFocusForward()
            }
            vm.squareFocusForward()
            resultKeys.add(v)
        }

        override fun onBackspaceClick() {
            if (squareInd == 0) {
                squareInFocus.setText("")
                resultKeys.remove(resultKeys.last())
            } else if (squareInFocus.text.toString() == "") {
                vm.squareFocusBackwards()
                squareInFocus.setText("")
                resultKeys.remove(resultKeys.last())
            } else {
                squareInFocus.setText("")
                resultKeys.remove(resultKeys.last())
            }
        }

        override fun onEnterClick() {
            currentRow.children.forEach { squareList.add(it as GuessBoard.Square) }
            val result = getResultString(squareList)
            if (vm.checkWord(result)) {
                vm.checkResult(result)
            }
            clearSquareList()
        }
    }

    private fun setupKeyboard() {
        keyBoard = findViewById<Keyboard>(R.id.keyboard_view)
        keyBoard.setupKeyboardClicks(KeyboardClicksIntImpl())
    }

    private fun setupScoresAndButton() {
        score1 = findViewById(R.id.player1)
        score2 = findViewById(R.id.player2)
        btnDone = findViewById(R.id.makeWord)
        btnDone.setOnClickListener { createSetWordDialog() }
    }

    private fun setupGuessBoard() {
        guessBoard = findViewById(R.id.guess_board)
        val lastRow = keyBoard.children.last() as Keyboard.Row
        enterKey = lastRow.children.last().apply { isEnabled = false } as Keyboard.EnterKey
    }

    private fun getResultString(result: List<GuessBoard.Square>): String {
        var resultString = ""
        result.forEach { resultString += it.text }
        return resultString
    }

    fun clearSquareList() {
        squareList = mutableListOf()
    }

    private fun makeShortToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun makeLongToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        vm.endRound()
        super.onDestroy()
    }
}