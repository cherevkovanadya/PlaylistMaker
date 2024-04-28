package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.App
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import com.example.playlistmaker.settings.data.SWITCH_KEY
import com.example.playlistmaker.settings.data.THEME_SETTINGS_PREFERENCES

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private lateinit var viewModel: SettingsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            SettingsViewModel.getViewModelFactory(applicationContext)
        )[SettingsViewModel::class.java]

        viewModel.getStateThemeLiveData().observe(this) {
            binding.themeSwitcher.isChecked = it
        }

        binding.returnBackImageView.setOnClickListener {
            finish()
        }

        binding.shareAppTextView.setOnClickListener {
            viewModel.shareApp()
        }

        binding.writeSupportTextView.setOnClickListener {
            viewModel.openSupport()
        }

        binding.termsOfUseTextView.setOnClickListener {
            viewModel.openTerms()
        }

        binding.themeSwitcher.setOnCheckedChangeListener { _, checked ->
            viewModel.switchTheme(checked)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.updateThemeSettings()
    }
}