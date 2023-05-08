package io.github.turskyi.travellingpro.features.travellers.view

import android.animation.ValueAnimator
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.ActivityTravellersBinding
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.features.countries.view.adapter.EmptyListObserver
import io.github.turskyi.travellingpro.features.traveller.view.TravellerActivity
import io.github.turskyi.travellingpro.features.travellers.TravellersActivityViewModel
import io.github.turskyi.travellingpro.features.travellers.view.adapter.TravellersAdapter
import io.github.turskyi.travellingpro.utils.Event
import io.github.turskyi.travellingpro.utils.extensions.hideKeyboard
import io.github.turskyi.travellingpro.utils.extensions.openActivityWithObject
import io.github.turskyi.travellingpro.utils.extensions.showKeyboard
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import io.github.turskyi.travellingpro.widgets.ExpandableSearchBar
import org.koin.android.ext.android.inject

class TravellersActivity : AppCompatActivity(), VisibilityDialog.VisibilityListener {
    companion object {
        const val EXTRA_TRAVELLER = "io.github.turskyi.travellingpro.TRAVELLER"
    }

    private val viewModel: TravellersActivityViewModel by inject()
    private val listAdapter: TravellersAdapter by inject()

    private lateinit var binding: ActivityTravellersBinding
    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            viewModel.searchQuery = s.toString()
            listAdapter.submitList(viewModel.pagedList)
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun afterTextChanged(s: Editable) {}
    }

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
        binding.expandableSearchBar.isFocusableInTouchMode = true
        window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
        listAdapter.submitList(viewModel.pagedList)
        binding.rvTravellers.adapter = listAdapter
        val layoutManager = GridLayoutManager(this, 2)
        binding.rvTravellers.layoutManager = layoutManager
    }

    private fun initListeners() {
        binding.expandableSearchBar.addTextChangeListener(textWatcher)
        binding.expandableSearchBar.onSearchActionListener =
            object : ExpandableSearchBar.OnSearchActionListener {
                override fun onSearchStateChanged(enabled: Boolean) {
                    if (enabled) {
                        expandSearch()
                    } else {
                        collapseSearch()
                    }
                }

                override fun onSearchConfirmed(text: String?) {}
                override fun onButtonClicked(buttonCode: Int) {}
            }

        binding.includeToolbar.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        listAdapter.onTravellerClickListener = ::showTraveller
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
                    infoDialog.show(
                        supportFragmentManager,
                        resources.getString(R.string.tag_visibility_dialog),
                    )
                }
            }
        }
    }

    private fun initObservers() {
        val observer = EmptyListObserver(binding.rvTravellers, binding.tvNoResults)
        listAdapter.registerAdapterDataObserver(observer)
        viewModel.topTravellersPercentLiveData.observe(this) { topPercent: Int ->
            updateTitle(topPercent)
        }
        viewModel.visibilityLoader.observe(this) { currentVisibility: Int ->
            binding.pb.visibility = currentVisibility
        }
        viewModel.visibilityUser.observe(this) { currentVisibility: Int ->
            if (currentVisibility == VISIBLE) {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_eye_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_eye_ripple
                listAdapter.submitList(viewModel.pagedList)
            } else {
                binding.floatBtnVisibility.setImageResource(R.drawable.btn_hide_ripple)
                binding.floatBtnVisibility.tag = R.drawable.btn_hide_ripple
                listAdapter.submitList(viewModel.pagedList)
            }
        }
        viewModel.errorMessage.observe(this) { event: Event<String> ->
            event.getMessageIfNotHandled()?.let { message: String ->
                toastLong(message)
            }
        }
    }

    private fun showTraveller(traveller: Traveller) {
        hideKeyboard()
        openActivityWithObject(TravellerActivity::class.java, EXTRA_TRAVELLER, traveller)
    }

    private fun updateTitle(percent: Int) {
        if (percent > 0) {
            binding.includeToolbar.tvToolbarTitle.text =
                getString(R.string.title_activity_travellers, percent)
        } else {
            binding.includeToolbar.tvToolbarTitle.text = getString(R.string.title_most)
        }
    }

    private fun collapseSearch() {
        binding.rvTravellers.animate()
            .translationY((-1 * resources.getDimensionPixelSize(R.dimen.offset_20)).toFloat())
        binding.expandableSearchBar.isSelected = false
        hideKeyboard()
        binding.includeToolbar.toolbar.animate().alpha(1f).duration = 200
        ValueAnimator.ofInt(
            0,
            binding.includeToolbar.toolbar.width
        ).apply {
            addUpdateListener {
                binding.includeToolbar.toolbar.layoutParams.width = animatedValue as Int
                binding.includeToolbar.toolbar.requestLayout()
                binding.includeToolbar.toolbar.clearFocus()
            }
            duration = 400
        }.start()
    }

    private fun expandSearch() {
        binding.rvTravellers.animate().translationY(0f)
        binding.expandableSearchBar.isSelected = true
        binding.includeToolbar.toolbar.animate().alpha(0f).duration = 200
        ValueAnimator.ofInt(
            0,
            binding.includeToolbar.toolbar.width
        ).apply {
            addUpdateListener {
                binding.includeToolbar.toolbar.layoutParams.width = animatedValue as Int
                binding.includeToolbar.toolbar.requestLayout()
            }
            doOnEnd {
                binding.includeToolbar.toolbar.requestFocus()
            }
            duration = 400
        }.start()
        showKeyboard()
    }
}