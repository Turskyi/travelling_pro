package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import io.github.turskyi.travellingpro.R
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
                ): Boolean {
                    return oldItem.name == newItem.name &&
                            oldItem.avatar == newItem.avatar &&
                            oldItem.id == newItem.id &&
                            oldItem.countryList == newItem.countryList
                }
            }
    }

    var onTravellerClickListener: ((country: Traveller) -> Unit)? = null

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

    inner class TravellerViewHolder(private val binding: ItemTravellerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(traveller: Traveller) {
            setSelectableItemBackground(this)
            binding.apply {
                Glide.with(itemView)
                    .load(traveller.avatar)
                    .into(ivAvatar)

                tvName.text = traveller.name
            }
        }

        private fun setSelectableItemBackground(holderTraveller: TravellerViewHolder) {
            val outValue = TypedValue()
            holderTraveller.itemView.context.theme.resolveAttribute(
                R.attr.selectableItemBackground,
                outValue,
                true
            )
        }
    }
}

