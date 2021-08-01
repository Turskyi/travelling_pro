package io.github.turskyi.travellingpro.features.traveller.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellerBinding
import io.github.turskyi.travellingpro.features.traveller.TravellerActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.TravellersActivity.Companion.EXTRA_TRAVELLER_ID
import io.github.turskyi.travellingpro.utils.extensions.toast
import org.koin.android.ext.android.inject

class TravellerActivity : AppCompatActivity() {
    private val viewModel: TravellerActivityViewModel by inject()
    private lateinit var binding: ActivityTravellerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTravellerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.includeAppBar.toolbar)

        val travellerId: String? = intent.getStringExtra(EXTRA_TRAVELLER_ID)
        if (travellerId != null) {
            viewModel.showListOfVisitedCountriesById(travellerId)
        } else {
            toast(R.string.msg_user_not_found)
            finish()
        }
    }
}