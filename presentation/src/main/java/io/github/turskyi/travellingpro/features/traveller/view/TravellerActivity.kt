package io.github.turskyi.travellingpro.features.traveller.view

import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellerBinding
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.features.traveller.TravellerActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.TravellersActivity.Companion.EXTRA_TRAVELLER
import io.github.turskyi.travellingpro.utils.decoration.SectionAverageGapItemDecoration
import io.github.turskyi.travellingpro.utils.extensions.getScreenWidth
import io.github.turskyi.travellingpro.utils.extensions.showSnackBar
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import org.koin.android.ext.android.inject

class TravellerActivity : AppCompatActivity() {
    private val viewModel: TravellerActivityViewModel by inject()
    private val listAdapter: VisitedCountriesAdapter by inject()

    private lateinit var binding: ActivityTravellerBinding
    private var traveller: Traveller? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        traveller = intent.getParcelableExtra(EXTRA_TRAVELLER)
        if (traveller != null) {
            initView()
            initListeners()
            initObservers()
        } else {
            toast(R.string.msg_user_not_found)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // makes info icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        if (viewModel.backPressedTiming + resources.getInteger(R.integer.desired_time_interval) > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            binding.root.showSnackBar(R.string.tap_back_button_in_order_to_exit)
        }
        viewModel.backPressedTiming = System.currentTimeMillis()
    }

    private fun initView() {
        binding = ActivityTravellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeAppBar.toolbar)
        binding.includeAppBar.toolbarLayout.title = traveller!!.name

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = adapter
            layoutManager = linearLayoutManager
            addItemDecoration(
                SectionAverageGapItemDecoration(
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_10),
                    resources.getDimensionPixelOffset(R.dimen.offset_20),
                    resources.getDimensionPixelOffset(R.dimen.offset_16),
                )
            )
        }
        initGravityForTitle()
        viewModel.showListOfVisitedCountriesById(traveller!!.id)
    }

    private fun initListeners() {
        listAdapter.apply {
            onFlagClickListener = { country ->
                // mis-clicking prevention, using threshold of 1000 ms
                if (SystemClock.elapsedRealtime() - viewModel.mLastClickTime > resources.getInteger(
                        R.integer.click_interval
                    )
                ) {
//              TODO: open Activity with flags of given user
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun initObservers() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { visitedCountries ->
            initTitleWithNumberOf(visitedCountries)
//           TODO: update adapter
        })
        viewModel.visitedCountries.observe(this, { visitedCountries ->
            binding.includeAppBar.circlePieChart.apply {
                initPieChart()
                createPieChartWith(visitedCountries, viewModel.notVisitedCountriesCount)
                binding.includeAppBar.circlePieChart.animatePieChart()
            }
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })

        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        })
    }

    private fun initGravityForTitle() {
        if (getScreenWidth() < 1082) binding.includeAppBar.toolbarLayout.expandedTitleGravity =
            Gravity.BOTTOM
    }

    private fun initTitleWithNumberOf(visitedCountryNodes: List<VisitedCountryNode>) {
//TODO: init title
    }
}