package com.example.playlistmaker.settings.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.databinding.ActivitySettingsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    private val viewModel: SettingsViewModel by viewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

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