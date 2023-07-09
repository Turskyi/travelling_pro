package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.content.res.ColorStateList
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.turskyi.travellingpro.databinding.ItemTravellerBinding
import io.github.turskyi.travellingpro.entities.Traveller

class TravellersAdapter : PagedListAdapter<Traveller, TravellersAdapter.TravellerViewHolder>(
    COUNTRIES_DIFF_CALLBACK
) {

    /**
     * Allows the RecyclerView to determine which items have changed when the [List] of [Traveller]
     * has been updated.
     */
    companion object {
        val COUNTRIES_DIFF_CALLBACK: DiffUtil.ItemCallback<Traveller> =
            object : DiffUtil.ItemCallback<Traveller>() {
                override fun areItemsTheSame(
                    oldItem: Traveller,
                    newItem: Traveller,
                ): Boolean = oldItem == newItem

                override fun areContentsTheSame(
                    oldItem: Traveller,
                    newItem: Traveller
                ): Boolean = oldItem.name == newItem.name &&
                        oldItem.avatar == newItem.avatar &&
                        oldItem.id == newItem.id
            }
    }

    var onTravellerClickListener: ((traveller: Traveller) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TravellerViewHolder {
        val binding: ItemTravellerBinding = ItemTravellerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false,
        )
        return TravellerViewHolder(binding)
    }

    override fun onBindViewHolder(holderTraveller: TravellerViewHolder, position: Int) {
        val currentItem: Traveller? = getItem(position)
        if (currentItem != null) {
            holderTraveller.bind(currentItem)
        }
    }

    inner class TravellerViewHolder(private val binding: ItemTravellerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cvTraveller.setOnClickListener {
                onTravellerClickListener?.invoke(getItem(layoutPosition) as Traveller)
            }
        }

        fun bind(traveller: Traveller) {
            setSelectableItemBackground(this)
            binding.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Glide.with(itemView).load(traveller.avatar).into(ivAvatar)
                    if (!traveller.isVisible) {
                        val tint: ColorStateList = ColorStateList.valueOf(
                            itemView.context.resources.getColor(
                                android.R.color.background_dark,
                                itemView.context.theme,
                            )
                        ).withAlpha(100)
                        cvTraveller.setCardBackgroundColor(tint)
                        ivAvatar.alpha = 0.4f
                    }
                } else {
                    cvAvatar.visibility = GONE
                    ivSquareAvatar.visibility = VISIBLE
                    Glide.with(itemView).load(traveller.avatar).into(ivSquareAvatar)
                }
                tvName.text = traveller.name
            }
        }

        private fun setSelectableItemBackground(holderTraveller: TravellerViewHolder) {
            holderTraveller.itemView.context.theme.resolveAttribute(
                io.github.turskyi.travellingpro.R.attr.selectableItemBackground,
                TypedValue(),
                true
            )
        }
    }
}

