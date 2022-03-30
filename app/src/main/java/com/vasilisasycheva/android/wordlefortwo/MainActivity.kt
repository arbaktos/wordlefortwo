package com.vasilisasycheva.android.wordlefortwo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.children
import com.vasilisasycheva.android.wordlefortwo.ui.WordleViewModel
import com.vasilisasycheva.android.wordlefortwo.ui.guessboard.GuessBoard
import com.vasilisasycheva.android.wordlefortwo.ui.guessboard.RowSeparate
import com.vasilisasycheva.android.wordlefortwo.ui.keyboard.*

class MainActivity : AppCompatActivity() {

    lateinit var currentRow: GuessBoard.RowOfSquares
    lateinit var squareInFocus: GuessBoard.Square
    lateinit var guessBoard: GuessBoard
    lateinit var keyBoard: Keyboard
    lateinit var score1: TextView
    lateinit var score2: TextView
    lateinit var btnDone: AppCompatButton

    private var squareInd = 0
    private var resultKeys: MutableList<Keyboard.TextKey> = mutableListOf()
    private val vm: WordleViewModel by viewModels()
    private var squareList: MutableList<GuessBoard.Square> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        score1 = findViewById(R.id.player1)
        score2 = findViewById(R.id.player2)
        btnDone = findViewById(R.id.makeWord)
        guessBoard = findViewById(R.id.guess_board)
        keyBoard = findViewById(R.id.keyboard_view)
        val lastRow = keyBoard.children.last() as Keyboard.Row
        val enterKey = lastRow.children.last() as Keyboard.EnterKey

        vm.currentRow.observe(this) {
            currentRow = guessBoard.getChildAt(it) as GuessBoard.RowOfSquares
        }
        vm.squareInFocus.observe(this) {
            squareInd = it
            squareInFocus = currentRow.getChildAt(it) as GuessBoard.Square
            enterKey.isEnabled = squareInd == 4
//            Log.d(DEBUG_TAG, "enter key enabled state: ${enterKey.isEnabled.toString()}")
        }
        vm.wordCheck.observe(this) {
            if (!it) {
                makeShortToast(getString(R.string.no_such_word_in_dict))
            }
        }
        vm.word.observe(this) {
//            if (it.isNullOrEmpty()) notWordState()
//            else withWordState()
        }
        vm.checkResult.observe(this) {
            displayCheckResults(it)
        }
        vm.isWin.observe(this) {
            if (it) createWinDialog()
        }
        vm.isLost.observe(this) {
            if (it) createLoseDialog()
        }
        vm.player1.observe(this) {
            score1.text = it.score.toString()
        }
        vm.player2.observe(this) {
            score2.text = it.score.toString()
        }
        btnDone.setOnClickListener {
            createSetWordDialog()
        }
        setupKeyboard()
    }

    private fun clearField() {
        keyBoard.children.forEach { row ->
            row as Keyboard.Row
            row.children.forEach { it as Key
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

    private fun notWordState() {
        keyBoard.isEnabled(false)
    }

    private fun withWordState() {
        btnDone.visibility = View.INVISIBLE
        keyBoard.isEnabled(true)
    }

    private fun createLoseDialog() {
        AlertDialog.Builder(this)
            .setMessage("Вы не отгадали слово :(")
            .setMessage("Слово этого раунда: ${vm.word.value}")
            .setPositiveButton("ОК") { _, _ ->
                clearField()
            }
            .show()
    }

    private fun createWinDialog() {
        AlertDialog.Builder(this)
            .setTitle("Слово отгадано!")
            .setPositiveButton("Ok") { _, _ ->
                clearField()
            }
            .show()
    }

    private fun createSetWordDialog() {
        val etWord = EditText(this)
//        val setWordRow = RowSeparate(this)
        AlertDialog.Builder(this)
            .setMessage("Загадай слово из пяти букв")
            .setView(etWord)
            .setPositiveButton("Готово") { _, _ ->
            val text = etWord.text.toString()
            if (vm.checkWord(text.uppercase())) vm.setWord(text)
        }.show()
    }

    inner class KeyboardClicksIntImpl: KeyboardClicksInt {
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
            checkInput(currentRow)
            currentRow.children.forEach { squareList.add(it as GuessBoard.Square) }
            val result = getResultString(squareList)
            if (vm.checkWord(result)) {
                vm.checkResult(result)
            }
            clearSquareList()
        }
    }

    private fun checkInput(currentRow: GuessBoard.RowOfSquares) {
        currentRow.children.forEach { it as GuessBoard.Square
            if (it.text.isNullOrEmpty()) makeShortToast("Загадайте слово из пяти букв")
        }
    }

    private fun displayCheckResults(checkResult: Map<GuessState, MutableMap<Int, Char>>) {
        resultKeys.forEach { textKey ->
            when(textKey.label.uppercase().first()) {
                in checkResult[GuessState.Positionmatch]!!.values -> textKey.setKeyState(GuessState.Positionmatch)//must be last?
                in checkResult[GuessState.Charmatch]!!.values -> textKey.setKeyState(GuessState.Charmatch)
                in checkResult[GuessState.Miss]!!.values -> textKey.setKeyState(GuessState.Miss)
            }
        }

        checkResult[GuessState.Miss]?.let { missMap ->
            missMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(GuessState.Miss)
            }
        }

        checkResult[GuessState.Charmatch]?.let { charMatchMap ->
            charMatchMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(GuessState.Charmatch)
            }
        }
        checkResult[GuessState.Positionmatch]?.let { posMatchMap ->
            posMatchMap.forEach { (ind, char) ->
                if (squareList[ind].text.toString() == char.toString()) squareList[ind].setSquareStatus(GuessState.Positionmatch)
            }
        }
    }

    private fun setupKeyboard() {
        val keyboard = findViewById<Keyboard>(R.id.keyboard_view)
        keyboard.setupKeyboardClicks(KeyboardClicksIntImpl())
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