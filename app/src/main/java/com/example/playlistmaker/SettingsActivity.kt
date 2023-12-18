package com.example.playlistmaker

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.databinding.ActivitySettingsBinding

const val THEME_SETTINGS_PREFERENCES = "theme_switcher_preferences"
const val SWITCH_KEY = "key_for_switch"

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        binding.themeSwitcher.isChecked = (applicationContext as App).darkTheme
        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            (applicationContext as App).switchTheme(checked)
            val sharedPrefs = getSharedPreferences(THEME_SETTINGS_PREFERENCES, MODE_PRIVATE)
            sharedPrefs.edit()
                .putBoolean(SWITCH_KEY, checked)
                .apply()
        }

        binding.shareAppTextView.setOnClickListener {
            Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, getString(R.string.practicum_android_developer))
                type = "text/plain"
                startActivity(Intent.createChooser(this, null))
            }
        }

        binding.writeSupportTextView.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.email_text))
            startActivity(shareIntent)
        }

        binding.termsOfUseTextView.setOnClickListener {
            val shareIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.yandex_terms_of_use)))
            startActivity(shareIntent)
        }
    }
}