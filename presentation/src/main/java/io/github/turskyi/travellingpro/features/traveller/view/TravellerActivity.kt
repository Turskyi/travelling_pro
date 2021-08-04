package io.github.turskyi.travellingpro.features.traveller.view

import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.Gravity
import io.github.turskyi.travellingpro.widgets.CirclePieChart
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellerBinding
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.entities.VisitedCountryNode
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity
import io.github.turskyi.travellingpro.features.traveller.TravellerActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.TravellersActivity.Companion.EXTRA_TRAVELLER
import io.github.turskyi.travellingpro.utils.decoration.SectionAverageGapItemDecoration
import io.github.turskyi.travellingpro.utils.extensions.*
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
            initObservers()
            initListeners()
        } else {
            toast(R.string.msg_user_not_found)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // makes back icon visible
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initView() {
        binding = ActivityTravellerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.includeAppBar.toolbar)
        binding.includeAppBar.toolbarLayout.title = traveller!!.name

        val linearLayoutManager = LinearLayoutManager(this)
        binding.rvVisitedCountries.apply {
            adapter = listAdapter
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.includeAppBar.cvAvatar.visibility = VISIBLE
            Glide.with(this).load(traveller!!.avatar).into(binding.includeAppBar.ivAvatar)
        } else {
            binding.includeAppBar.cvAvatar.visibility = GONE
            binding.includeAppBar.ivSquareAvatar.visibility = VISIBLE
            Glide.with(this).load(traveller!!.avatar).into(
                binding.includeAppBar.ivSquareAvatar,
            )
        }
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
                    openActivityWithArgs(FlagsActivity::class.java) {
                        putParcelable(FlagsActivity.EXTRA_USER, traveller)
                        putInt(FlagsActivity.EXTRA_POSITION, getItemPosition(country))
                        if (viewModel.visitedCountries.value != null) {
                            putInt(
                                FlagsActivity.EXTRA_ITEM_COUNT,
                                viewModel.visitedCountries.value!!.size
                            )
                        } else {
                            putInt(FlagsActivity.EXTRA_ITEM_COUNT, traveller!!.counter)
                        }
                    }
                }
                viewModel.mLastClickTime = SystemClock.elapsedRealtime()
            }
        }
    }

    private fun initObservers() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { visitedCountries ->
            initTitle()
            updateAdapterWith(visitedCountries)
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

    /** [showTitleWithOnlyCountries] function must be open
     *  to use it in custom [CirclePieChart] widget */
    fun showTitleWithOnlyCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { visitedCountryNodes ->
            binding.includeAppBar.toolbarLayout.title = resources.getString(
                R.string.name_and_counter, traveller!!.name, resources.getQuantityString(
                    R.plurals.travellerCounter,
                    visitedCountryNodes.size,
                    visitedCountryNodes.size
                )
            )
        })
    }

    private fun showTitleWithCitiesAndCountries() {
        viewModel.visitedCountriesWithCitiesNode.observe(this, { visitedCountryNodes ->
            if (viewModel.citiesCount > visitedCountryNodes.size) {
                binding.includeAppBar.toolbarLayout.title = resources.getString(
                    R.string.name_and_counter, traveller!!.name, "${
                        resources.getQuantityString(
                            R.plurals.travellerCitiesVisited,
                            viewModel.citiesCount,
                            viewModel.citiesCount
                        )
                    } ${
                        resources.getQuantityString(
                            R.plurals.numberOfCountriesOfCitiesVisited, visitedCountryNodes.size,
                            visitedCountryNodes.size
                        )
                    }"
                )
            } else {
                binding.includeAppBar.toolbarLayout.title = resources.getString(
                    R.string.name_and_counter, traveller!!.name, "${
                        resources.getQuantityString(
                            R.plurals.travellerCitiesVisited,
                            visitedCountryNodes.size,
                            visitedCountryNodes.size
                        )
                    } ${
                        resources.getQuantityString(
                            R.plurals.numberOfCountriesOfCitiesVisited,
                            visitedCountryNodes.size,
                            visitedCountryNodes.size
                        )
                    }"
                )
            }
        })
    }

    private fun initTitle() {
        if (viewModel.citiesCount == 0) {
            showTitleWithOnlyCountries()
        } else {
            showTitleWithCitiesAndCountries()
        }
    }

    private fun updateAdapterWith(visitedCountryNodes: List<VisitedCountryNode>) {
        // makes all list items collapsed
        for (countryNode in visitedCountryNodes) {
            countryNode.isExpanded = false
        }
        listAdapter.setList(visitedCountryNodes)
    }
}