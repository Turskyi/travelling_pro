package io.github.turskyi.travellingpro.features.travellers.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellersBinding
import io.github.turskyi.travellingpro.features.allcountries.view.adapter.EmptyListObserver
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersAdapter
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.features.traveller.view.TravellerActivity
import io.github.turskyi.travellingpro.utils.extensions.*
import org.koin.android.ext.android.inject

class TravellersActivity : AppCompatActivity(), VisibilityDialog.VisibilityListener {
    companion object {
        const val EXTRA_TRAVELLER_ID = "io.github.turskyi.travellingpro.TRAVELLER_ID"
    }

    private val viewModel: TravellersActivityViewModel by inject()
    private val adapter: TravellersAdapter by inject()

    private lateinit var binding: ActivityTravellersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
        initObservers()
    }

    /**
     * Calling when user clicks "[R.string.dialog_btn_ok_ready]" button in [VisibilityDialog].
     */
    override fun becomeVisible() {
        viewModel.onBecomingVisibleTriggered()
    }

    private fun initView() {
        binding = ActivityTravellersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etSearch.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        adapter.submitList(viewModel.pagedList)
        binding.rvTravellers.adapter = adapter
        val layoutManager = GridLayoutManager(this, 2)
        binding.rvTravellers.layoutManager = layoutManager
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

        binding.includeToolbar.toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onTravellerClickListener = ::showTraveller
        binding.rvTravellers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> binding.floatBtnVisibility.show()
                    dy < 0 -> binding.floatBtnVisibility.hide()
                }
            }
        })
        binding.floatBtnVisibility.apply {
            setOnClickListener {
                val isUserVisible: Boolean = tag is Int && (tag as Int) == R.drawable.btn_eye_ripple
                if (isUserVisible) {
                    viewModel.onVisibilityFabClicked()
                } else {
                    val infoDialog: VisibilityDialog = VisibilityDialog.newInstance(
                        getString(R.string.txt_info_all_travellers)
                    )
                    infoDialog.show(supportFragmentManager, "visibility dialog")
                }
            }
        }
    }

    private fun initObservers() {
        val observer = EmptyListObserver(binding.rvTravellers, binding.tvNoResults)
        adapter.registerAdapterDataObserver(observer)
        viewModel.topTravellersPercentLiveData.observe(this, { topPercent ->
            updateTitle(topPercent)
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            binding.pb.visibility = currentVisibility
        })
        viewModel.visibilityUser.observe(this, { currentVisibility ->
            if (currentVisibility == VISIBLE) {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_eye_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_eye_ripple
            } else {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_hide_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_hide_ripple
            }
        })
        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        })
    }

    private fun showTraveller(traveller: Traveller) {
        hideKeyboard()
        openActivityWithArgs(TravellerActivity::class.java) {
            putString(EXTRA_TRAVELLER_ID, traveller.id)
        }
    }

    private fun updateTitle(percent: Int) {
        binding.includeToolbar.tvToolbarTitle.text = getString(R.string.title_activity_travellers, percent)
    }

    private fun collapseSearch() {
        binding.rvTravellers.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        binding.ibSearch.isSelected = false
        val width: Int = binding.includeToolbar.toolbar.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        hideKeyboard()
        binding.etSearch.setText("")
        binding.includeToolbar.tvToolbarTitle.animate().alpha(1f).duration = 200
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
        binding.rvTravellers.animate().translationY(0f)
        binding.ibSearch.isSelected = true
        val width: Int = binding.includeToolbar.toolbar.width - resources.getDimensionPixelSize(R.dimen.offset_16)
        binding.includeToolbar.tvToolbarTitle.animate().alpha(0f).duration = 200
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