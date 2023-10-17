package com.example.playlistmaker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.text.Layout
import android.view.FrameMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.ContentFrameLayout
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val returnButton = findViewById<ImageView>(R.id.return_back)
        returnButton.setOnClickListener {
            val returnIntent = Intent(this, MainActivity::class.java)
            startActivity(returnIntent)
        }

        val switchThemeButton = findViewById<SwitchCompat>(R.id.switch_theme)
//        val sharedPreferences = getSharedPreferences("MODE", MODE_PRIVATE)
//        val editor = sharedPreferences.edit()
//        val nightTheme = sharedPreferences.getBoolean("night",false)
//
//        if(nightTheme){
//            switchThemeButton.isChecked = true
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        }
//
//        switchThemeButton.setOnCheckedChangeListener { _, isChecked ->
//            if(!isChecked){
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//                editor.putBoolean("night",false)
//            }
//            else {
//                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//                editor.putBoolean("night",true)
//            }
//            editor.apply()
//        }

        val writeSupportButton = findViewById<View>(R.id.write_support)
        writeSupportButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))
            startActivity(shareIntent)
        }

        val termsOfUseButton = findViewById<View>(R.id.terms_of_use)
        termsOfUseButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.yandex_terms_of_use)))
            startActivity(shareIntent)
        }
    }
}