package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.content.Context
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.RequestOptions
import com.github.twocoffeesoneteam.glidetovectoryou.GlideApp
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.FragmentFlagBinding
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.view.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.callbacks.OnChangeFlagFragmentListener
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.widgets.ListenableWebView

open class BaseFlagFragment : Fragment() {

    private var _binding: FragmentFlagBinding? = null
    val binding get() = _binding!!

    var mChangeFlagListener: OnChangeFlagFragmentListener? = null
    var flagsActivityViewListener: FlagsActivityView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnChangeFlagFragmentListener) {
            mChangeFlagListener = context
        } else {
            toast(getString(R.string.msg_exception_flag_listener, context))
        }
        try {
            flagsActivityViewListener = context as FlagsActivityView
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

    fun showFlagClickListener(countries: List<VisitedCountry>, position: Int):
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
        // returns first clickListener
        binding.ivEnlargedFlag.setOnClickListener(showFlagClickListener(countries, position))
    }

    fun showSelfie(countries: List<VisitedCountry>, position: Int) {
        binding.apply {
            ivEnlargedFlag.visibility = VISIBLE
            wvFlag.visibility = GONE
        }
        val thumbnailBuilder: RequestBuilder<Drawable> =
            GlideApp.with(binding.ivEnlargedFlag.context)
                .asDrawable()
                .sizeMultiplier(ResourcesCompat.getFloat(resources, R.dimen.thumbnail))
        GlideApp.with(this@BaseFlagFragment)
            .load(countries[position].selfie)
            .thumbnail(thumbnailBuilder)
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
    fun showTheFlag(countries: List<VisitedCountry>, position: Int) {
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