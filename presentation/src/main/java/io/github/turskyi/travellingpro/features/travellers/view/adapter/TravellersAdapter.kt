package io.github.turskyi.travellingpro.features.travellers.view.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import io.github.turskyi.travellingpro.R
import io.github.turskyi.travellingpro.models.Traveller

class TravellersAdapter : PagedListAdapter<Traveller, TravellersAdapter.ViewHolder>(
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context).inflate(R.layout.item_traveller, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        TODO: implement onBindViewHolder
    }

    private fun setSelectableItemBackground(holder: ViewHolder) {
        val outValue = TypedValue()
        holder.itemView.context.theme.resolveAttribute(
            R.attr.selectableItemBackground,
            outValue,
            true
        )
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//TODO: implement view holder
    }
}

