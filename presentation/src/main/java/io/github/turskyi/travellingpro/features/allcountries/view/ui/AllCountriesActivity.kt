package io.github.turskyi.travellingpro.features.allcountries.view.ui

import android.animation.ValueAnimator
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityAllCountriesBinding
import io.github.turskyi.travellingpro.utils.extensions.openInfoDialog
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.AllCountriesAdapter
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.EmptyListObserver
import io.github.turskyi.travellingpro.features.allcountries.viewmodel.AllCountriesActivityViewModel
import io.github.turskyi.travellingpro.models.Country
import io.github.turskyi.travellingpro.utils.extensions.hideKeyboard
import io.github.turskyi.travellingpro.utils.extensions.showKeyboard
import org.koin.android.ext.android.inject

class AllCountriesActivity : AppCompatActivity() {

    private val viewModel: AllCountriesActivityViewModel by inject()
    private val adapter: AllCountriesAdapter by inject()
    private lateinit var binding: ActivityAllCountriesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun initView() {
        binding = ActivityAllCountriesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etSearch.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        adapter.submitList(viewModel.pagedList)
        val layoutManager = LinearLayoutManager(this)
        binding.rvAllCountries.adapter = adapter
        binding.rvAllCountries.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.ibSearch.setOnClickListener { search ->
            if (search.isSelected) {
                collapseSearch()
            } else {
                expandSearch()
            }
        }
        binding.etSearch.addTextChangedListener { inputText ->
            viewModel.searchQuery = inputText.toString()
            adapter.submitList(viewModel.pagedList)
        }

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onCountryClickListener = ::addToVisited
        adapter.onCountryLongClickListener = ::sendToGoogleMapToShowGeographicalLocation
        binding.rvAllCountries.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> binding.floatBtnInfo.show()
                    dy < 0 -> binding.floatBtnInfo.hide()
                }
            }
        })
        binding.floatBtnInfo.setOnClickListener { openInfoDialog(getString(R.string.txt_info_all_countries)) }
    }

    private fun initObservers() {
        val observer = EmptyListObserver(binding.rvAllCountries, binding.tvNoResults)
        adapter.registerAdapterDataObserver(observer)
        viewModel.notVisitedCountriesNumLiveData.observe(this, { notVisitedNum ->
            updateTitle(notVisitedNum)
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

    private fun sendToGoogleMapToShowGeographicalLocation(country: Country) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.geo_location, country.name))
        )
        startActivity(intent)
    }

    private fun addToVisited(country: Country) {
        viewModel.markAsVisited(country) {
            hideKeyboard()
            val intent = Intent()
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun updateTitle(num: Int) {
        binding.tvToolbarTitle.text =
            resources.getQuantityString(R.plurals.numberOfCountriesRemain, num, num)
    }

    private fun collapseSearch() {
        binding.rvAllCountries.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        binding.ibSearch.isSelected = false
        val width =
            binding.toolbar.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        hideKeyboard()
        binding.etSearch.setText("")
        binding.tvToolbarTitle.animate().alpha(1f).duration = 200
        binding.sllSearch.elevate(
            resources.getDimension(R.dimen.elevation_8),
            resources.getDimension(R.dimen.elevation_1),
            100
        )
        ValueAnimator.ofInt(
            width,
            0
        ).apply {
            addUpdateListener {
                binding.etSearch.layoutParams.width = animatedValue as Int
                binding.sllSearch.requestLayout()
                binding.sllSearch.clearFocus()
            }
            duration = 400
        }.start()
    }

    private fun expandSearch() {
        binding.rvAllCountries.animate().translationY(0f)
        binding.ibSearch.isSelected = true
        val width = binding.toolbar.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        binding.tvToolbarTitle.animate().alpha(0f).duration = 200
        binding.sllSearch.elevate(
            resources.getDimension(R.dimen.elevation_1),
            resources.getDimension(R.dimen.elevation_8),
            100
        )
        ValueAnimator.ofInt(
            0,
            width
        ).apply {
            addUpdateListener {
                binding.etSearch.layoutParams.width = animatedValue as Int
                binding.sllSearch.requestLayout()
            }
            doOnEnd {
                binding.etSearch.requestFocus()
                binding.etSearch.setText("")
            }
            duration = 400
        }.start()
        showKeyboard()
    }
}