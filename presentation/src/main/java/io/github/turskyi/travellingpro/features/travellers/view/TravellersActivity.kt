package io.github.turskyi.travellingpro.features.travellers.view

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellersBinding
import io.github.turskyi.travellingpro.features.home.view.ui.HomeActivity
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersAdapter
import io.github.turskyi.travellingpro.models.Traveller
import io.github.turskyi.travellingpro.utils.extensions.hideKeyboard
import io.github.turskyi.travellingpro.utils.extensions.openActivityWithArgs
import io.github.turskyi.travellingpro.utils.extensions.openInfoDialog
import io.github.turskyi.travellingpro.utils.extensions.showKeyboard
import org.koin.android.ext.android.inject

class TravellersActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_TRAVELLER = "io.github.turskyi.travellingpro.TRAVELLER"
    }
    private val viewModel: TravellersActivityViewModel by inject()
    private val adapter: TravellersAdapter by inject()

    private lateinit var binding: ActivityTravellersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListeners()
    }

    private fun initView() {
        binding = ActivityTravellersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.etSearch.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        adapter.submitList(viewModel.pagedList)
        binding.rvTravellers.adapter = adapter
        val layoutManager = GridLayoutManager(this,2)
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

        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
        adapter.onTravellerClickListener = ::showTraveller
        binding.rvTravellers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> binding.floatBtnInfo.show()
                    dy < 0 -> binding.floatBtnInfo.hide()
                }
            }
        })
        binding.floatBtnInfo.setOnClickListener { openInfoDialog(getString(R.string.txt_info_all_travellers)) }
    }

    private fun showTraveller(traveller: Traveller) {
        hideKeyboard()
        openActivityWithArgs(HomeActivity::class.java) {
            putString(EXTRA_TRAVELLER, traveller.id)
        }
    }

    private fun collapseSearch() {
        binding.rvTravellers.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        binding.ibSearch.isSelected = false
        val width: Int = binding.toolbar.width - resources.getDimensionPixelSize(R.dimen.offset_16)
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
        binding.rvTravellers.animate().translationY(0f)
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