package com.example.lost_found_app.ui
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.lost_found_app.databinding.ItemLostFoundBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LostFoundAdapter(
    private val onItemClick: (LostFoundItem) -> Unit
) : ListAdapter<LostFoundItem, LostFoundAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemLostFoundBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LostFoundItem) {
            binding.tvItemTitle.text = "${item.postType} ${item.description.take(30)}..."

            binding.tvCategory.text = item.category

            binding.tvTimeAgo.text = getTimeAgo(item.postedAt)

            binding.tvLocation.text = item.location


            if (!item.imagePath.isNullOrEmpty()) {
                binding.ivItemImage.setImageURI(Uri.parse(item.imagePath))
            } else {
                binding.ivItemImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            // Tapping the card navigates to the detail screen
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLostFoundBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }


    class DiffCallback : DiffUtil.ItemCallback<LostFoundItem>() {
        override fun areItemsTheSame(old: LostFoundItem, new: LostFoundItem) = old.id == new.id
        override fun areContentsTheSame(old: LostFoundItem, new: LostFoundItem) = old == new
    }


    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)} minutes ago"
            diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)} hours ago"
            diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)} days ago"
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(timestamp))
        }
    }
}