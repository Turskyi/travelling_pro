@file:Suppress("DEPRECATION")

package io.github.turskyi.travellingpro.features.countries.view.adapter

import android.graphics.Paint
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.net.toUri
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYou
import com.github.twocoffeesoneteam.glidetovectoryou.GlideToVectorYouListener
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.entities.Country
import io.github.turskyi.travellingpro.utils.extensions.isHttpUrl
import io.github.turskyi.travellingpro.utils.extensions.loadFlagFallback
import io.github.turskyi.travellingpro.utils.extensions.showFlagText
import io.github.turskyi.travellingpro.utils.getDisplayName

class AllCountriesAdapter :
    PagedListAdapter<Country, AllCountriesAdapter.CountryViewHolder>(
        COUNTRIES_DIFF_CALLBACK
    ) {

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Country]
     * has been updated.
     */
    companion object {
        val COUNTRIES_DIFF_CALLBACK: DiffUtil.ItemCallback<Country> =
            object : DiffUtil.ItemCallback<Country>() {
                override fun areItemsTheSame(
                    oldItem: Country,
                    newItem: Country
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: Country,
                    newItem: Country
                ): Boolean =
                    oldItem.name == newItem.name && oldItem.isVisited == newItem.isVisited
            }
    }

    var onCountryClickListener: ((country: Country) -> Unit)? = null
    var onCountryLongClickListener: ((country: Country) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CountryViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.item_list_country, parent, false)
        return CountryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CountryViewHolder, position: Int) {
        val currentCountry: Country? = getItem(position)
        if (currentCountry != null) {
            holder.bind(currentCountry)
        }
    }

    private fun showPicturesInSVG(
        country: Country,
        holder: CountryViewHolder
    ) {
        if (!country.flag.isHttpUrl()) {
            holder.ivFlag.visibility = GONE
            holder.wvFlag.visibility = GONE
            holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback)
                .showFlagText(country.flag)
            return
        }
        holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback).visibility = GONE
        holder.ivFlag.visibility = VISIBLE
        holder.wvFlag.visibility = GONE
        val uri: Uri = country.flag.toUri()

        GlideToVectorYou
            .init()
            .with(holder.itemView.context)
            .withListener(object : GlideToVectorYouListener {
                override fun onLoadFailed() = showPicturesInWebView(
                    holder, country,
                )

                override fun onResourceReady() {
                    holder.ivFlag.visibility = VISIBLE
                    holder.wvFlag.visibility = GONE
                }
            })
            .setPlaceHolder(R.drawable.anim_loading, R.drawable.ic_broken_image)
            .load(uri, holder.ivFlag)
    }

    private fun showPicturesInWebView(
        holder: CountryViewHolder,
        country: Country
    ) {
        holder.ivFlag.visibility = GONE
        holder.itemView.findViewById<TextView>(R.id.tv_flag_fallback).visibility = GONE
        holder.wvFlag.loadFlagFallback(country.flag)
    }

    inner class CountryViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val tvCountry: TextView = itemView.findViewById(R.id.tv_country)
        val ivFlag: ImageView = itemView.findViewById(R.id.iv_flag)
        val wvFlag: WebView = itemView.findViewById(R.id.wv_flag)
        private val llCountry: LinearLayout =
            itemView.findViewById(R.id.ll_country)

        init {
            itemView.setOnClickListener {
                onCountryClickListener?.invoke(getItem(layoutPosition) as Country)
            }
            itemView.setOnLongClickListener {
                onCountryLongClickListener?.invoke(getItem(layoutPosition) as Country)
                true
            }
        }

        fun bind(country: Country) {
            tvCountry.text = country.getDisplayName(tvCountry.context)
            setSelectableItemBackground(this)
            if (country.isVisited) {
                tvCountry.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                tvCountry.paintFlags =
                    tvCountry.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
            showPicturesInSVG(country, this)
        }

        private fun setSelectableItemBackground(holder: CountryViewHolder) {
            val outValue = TypedValue()
            holder.itemView.context.theme.resolveAttribute(
                R.attr.selectableItemBackground,
                outValue,
                true
            )
            holder.llCountry.setBackgroundResource(outValue.resourceId)
        }
    }
}
