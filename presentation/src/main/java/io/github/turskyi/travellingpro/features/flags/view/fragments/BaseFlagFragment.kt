package io.github.turskyi.travellingpro.features.flags.view.fragments

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.OnClickListener
import android.view.View.OnTouchListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.twocoffeesoneteam.glidetovectoryou.GlideApp
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import com.google.firebase.storage.FirebaseStorage
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.databinding.FragmentFlagBinding
import io.github.turskyi.travellingpro.entities.VisitedCountry
import io.github.turskyi.travellingpro.features.flags.view.callbacks.FlagsActivityView
import io.github.turskyi.travellingpro.features.flags.view.callbacks.OnChangeFlagFragmentListener
import io.github.turskyi.travellingpro.utils.extensions.isHttpUrl
import io.github.turskyi.travellingpro.utils.extensions.loadFlagFallback
import io.github.turskyi.travellingpro.utils.extensions.showFlagText
import io.github.turskyi.travellingpro.utils.extensions.toast
import io.github.turskyi.travellingpro.widgets.ListenableWebView

open class BaseFlagFragment : Fragment() {

    private var _binding: FragmentFlagBinding? = null
    val binding: FragmentFlagBinding get() = _binding!!

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
        binding.tvFlagFallback.setOnClickListener {
            showSelfie(countries, position)
            binding.ivEnlargedFlag.setOnClickListener(
                showFlagClickListener(countries, position)
            )
        }
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
        val selfie = countries[position].selfie
        if (selfie.isBlank()) {
            showTheFlag(countries, position)
            return
        }
        binding.apply {
            ivEnlargedFlag.visibility = VISIBLE
            wvFlag.visibility = GONE
            tvFlagFallback.visibility = GONE
        }
        if (selfie.isHttpUrl()) {
            loadSelfie(selfie, countries, position)
            return
        }
        FirebaseStorage.getInstance().reference
            .child(selfie.trimStart('/'))
            .downloadUrl
            .addOnSuccessListener { downloadUrl ->
                if (isAdded && _binding != null) {
                    loadSelfie(downloadUrl.toString(), countries, position)
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Failed to resolve selfie from Firebase Storage", exception)
                if (isAdded && _binding != null) {
                    showTheFlag(countries, position)
                }
            }
    }

    private fun loadSelfie(selfieUrl: String, countries: List<VisitedCountry>, position: Int) {
        val thumbnailBuilder: RequestBuilder<Drawable> =
            GlideApp.with(binding.ivEnlargedFlag.context)
                .asDrawable()
                .sizeMultiplier(ResourcesCompat.getFloat(resources, R.dimen.thumbnail))
        GlideApp.with(this@BaseFlagFragment)
            .load(selfieUrl)
            .thumbnail(thumbnailBuilder)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.anim_loading)
                    .error(R.drawable.ic_broken_image)
                    .priority(Priority.IMMEDIATE)
            )
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.ivEnlargedFlag.post {
                        if (isAdded) {
                            showTheFlag(countries, position)
                        }
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable>,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(binding.ivEnlargedFlag)
    }

    /**
     * @Description Opens the pictureUri in full size
     *  */
    fun showTheFlag(countries: List<VisitedCountry>, position: Int) {
        val flag = countries[position].flag
        if (!flag.isHttpUrl()) {
            binding.ivEnlargedFlag.visibility = GONE
            binding.wvFlag.visibility = GONE
            binding.tvFlagFallback.showFlagText(flag)
            return
        }
        binding.tvFlagFallback.visibility = GONE
        binding.ivEnlargedFlag.visibility = VISIBLE
        binding.wvFlag.visibility = GONE
        val uri = flag.toUri()
        GlideToVectorYou
            .init()
            .with(activity)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() = showFlagInWebView(flag)

                override fun onResourceReady() {
                    binding.apply {
                        ivEnlargedFlag.visibility = VISIBLE
                        wvFlag.visibility = GONE
                        tvFlagFallback.visibility = GONE
                    }
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, binding.ivEnlargedFlag)
    }

    private fun showFlagInWebView(flag: String) {
        binding.ivEnlargedFlag.visibility = GONE
        binding.tvFlagFallback.visibility = GONE
        binding.wvFlag.loadFlagFallback(flag)
    }

    private companion object {
        const val TAG = "BaseFlagFragment"
    }
}