package io.github.turskyi.travellingpro.features.flags.view.fragment

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.request.RequestOptions
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.FragmentFlagBinding
import io.github.turskyi.travellingpro.entities.Traveller
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.callbacks.OnChangeFlagFragmentListener
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_POSITION
import io.github.turskyi.travellingpro.features.flags.view.FlagsActivity.Companion.EXTRA_USER
import io.github.turskyi.travellingpro.features.flags.viewmodel.FriendFlagsFragmentViewModel
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.utils.extensions.toastLong
import io.github.turskyi.travellingpro.widgets.ListenableWebView
import org.koin.android.ext.android.inject

class FriendFlagsFragment : Fragment() {

    private var _binding: FragmentFlagBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FriendFlagsFragmentViewModel by inject()

    var mChangeFlagListener: OnChangeFlagFragmentListener? = null
    private var flagsActivityViewListener: FlagsActivityView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChangeFlagFragmentListener) {
            mChangeFlagListener = context
        } else {
            toast(getString(R.string.msg_exception_flag_listener, context))
        }
        try {
            flagsActivityViewListener = context as FlagsActivityView?
        } catch (castException: ClassCastException) {
            // in this case the activity does not implement the listener.
            toast(castException.localizedMessage ?: castException.stackTraceToString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlagBinding.inflate(inflater, container, false)
        if (this.arguments != null && this.requireArguments()
                .getParcelable<Traveller>(EXTRA_USER) != null
        ) {
            val traveller: Traveller = this.requireArguments().getParcelable(EXTRA_USER)!!
            viewModel.setVisitedCountries(traveller.id)
        } else {
            toast(R.string.msg_not_found)
            requireActivity().finish()
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDetach() {
        super.onDetach()
        mChangeFlagListener = null
        flagsActivityViewListener = null
    }

    override fun onResume() {
        super.onResume()
        initObservers()
    }

    private fun initObservers() {
        viewModel.errorMessage.observe(this, { event ->
            event.getMessageIfNotHandled()?.let { message ->
                toastLong(message)
            }
        })
        viewModel.visibilityLoader.observe(this, { currentVisibility ->
            flagsActivityViewListener?.setLoaderVisibility(currentVisibility)
        })
        val visitedCountriesObserver: Observer<List<VisitedCountry>> =
            Observer<List<VisitedCountry>> { countries ->
                val position: Int = this.requireArguments().getInt(EXTRA_POSITION)
                if(mChangeFlagListener != null){
                    mChangeFlagListener!!.onChangeToolbarTitle(countries[position].title)
                    if (countries[position].selfie.isEmpty()) {
                        showTheFlag(countries, position)
                    } else {
                        showSelfie(countries, position)
                        binding.ivEnlargedFlag.setOnClickListener(
                            showFlagClickListener(countries, position)
                        )
                    }
                } else {
                    toast(R.string.msg_not_found)
                    requireActivity().finish()
                }
            }
        viewModel.visitedCountries.observe(viewLifecycleOwner, visitedCountriesObserver)
    }

    private fun showFlagClickListener(countries: List<VisitedCountry>, position: Int):
            OnClickListener = OnClickListener {
        showTheFlag(countries, position)
        // change clickListener
        binding.ivEnlargedFlag.setOnClickListener(showSelfieClickListener(countries, position))
        val wvFlag: ListenableWebView = binding.wvFlag
        wvFlag.setOnTouchListener(onWebViewClickListener(countries, position))
    }

    private fun onWebViewClickListener(
        countries: List<VisitedCountry>,
        position: Int
    ): OnTouchListener {
        return OnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_UP -> {
                    // perform click
                    showSelfie(countries, position)
                    view.performClick()
                    // return first clickListener
                    binding.ivEnlargedFlag.setOnClickListener(
                        showFlagClickListener(
                            countries,
                            position
                        )
                    )
                }
            }
            false
        }
    }

    private fun showSelfieClickListener(countries: List<VisitedCountry>, position: Int):
            OnClickListener = OnClickListener {
        showSelfie(countries, position)
        // return first clickListener
        binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
    }

    private fun showSelfie(countries: List<VisitedCountry>, position: Int) {
        binding.apply {
            ivEnlargedFlag.visibility = VISIBLE
            wvFlag.visibility = GONE
        }
        val uri: Uri = Uri.parse(countries[position].selfie)
        Glide.with(this)
            .load(uri)
            .thumbnail(0.5F)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.anim_loading)
                    .error(R.drawable.ic_broken_image)
                    .priority(Priority.IMMEDIATE)
            )
            .into(binding.ivEnlargedFlag)
    }

    /**
     * @Description Opens the pictureUri in full size
     *  */
    private fun showTheFlag(countries: List<VisitedCountry>, position: Int) {
        val uri: Uri = Uri.parse(countries[position].flag)
        GlideToVectorYou
            .init()
            .with(activity)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() = showFlagInWebView()
                private fun showFlagInWebView() {
                    binding.apply {
                        ivEnlargedFlag.visibility = GONE
                        wvFlag.apply {
                            webViewClient = WebViewClient()
                            visibility = VISIBLE
                            setBackgroundColor(TRANSPARENT)
                            loadData(
                                getString(R.string.html_data_flag, countries[position].flag),
                                getString(R.string.mime_type_txt_html),
                                getString(R.string.encoding_utf_8)
                            )
                        }
                    }
                }

                override fun onResourceReady() {
                    binding.apply {
                        ivEnlargedFlag.visibility = VISIBLE
                        wvFlag.visibility = GONE
                    }
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, binding.ivEnlargedFlag)
    }
}