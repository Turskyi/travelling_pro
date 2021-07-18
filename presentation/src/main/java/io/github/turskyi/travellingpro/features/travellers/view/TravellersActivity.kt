package io.github.turskyi.travellingpro.features.travellers.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import io.github.turskyi.travellingpro.databinding.ActivityTravellersBinding
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import org.koin.android.ext.android.inject

class TravellersActivity : AppCompatActivity() {
    private val viewModel: TravellersActivityViewModel by inject()
    private lateinit var binding: ActivityTravellersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTravellersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etSearch.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        binding.toolbarLayout.title = title
    }
}