package io.github.turskyi.travellingpro.features.traveller

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.turskyi.travellingpro.databinding.ActivityTravellerBinding

class TravellerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTravellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTravellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.includeAppBar.toolbar)
    }
}