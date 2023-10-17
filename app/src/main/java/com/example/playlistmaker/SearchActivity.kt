package com.example.playlistmaker

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity


class SearchActivity : AppCompatActivity() {

    private var searchText: String = TEXT_DEF
    private lateinit var inputEditText: EditText

    companion object {
        const val TEXT_DEF = ""
        const val SEARCH_TEXT = "SEARCH_TEXT"
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT, searchText)
        Log.e("text1",searchText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        inputEditText = findViewById<EditText>(R.id.input_edit_text)
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString(SEARCH_TEXT, TEXT_DEF)
            Log.e("text2",searchText)
            inputEditText.setText(searchText)
        }

        val returnButton = findViewById<ImageView>(R.id.return_back)
        returnButton.setOnClickListener {
            val returnIntent = Intent(this, MainActivity::class.java)
            startActivity(returnIntent)
        }

        val clearButton = findViewById<ImageView>(R.id.clear_icon)
        clearButton.setOnClickListener {
            inputEditText.setText("")
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(inputEditText.getWindowToken(), 0)
        }

        val simpleTextWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearButton.visibility = clearButtonVisibility(s)
                searchText = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        inputEditText.addTextChangedListener(simpleTextWatcher)
    }
    private fun clearButtonVisibility(s: CharSequence?): Int {
        return if (s.isNullOrEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }
}