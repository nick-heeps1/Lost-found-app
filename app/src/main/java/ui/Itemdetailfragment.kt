package com.example.lost_found_app.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.lost_found_app.databinding.FragmentItemDetailBinding
import java.text.SimpleDateFormat
import java.util.*

class ItemDetailFragment : Fragment() {

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LostFoundViewModel by activityViewModels()

    private val args: ItemDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.allItems.observe(viewLifecycleOwner) { items ->
            val item = items.find { it.id == args.itemId } ?: return@observe

            binding.tvDetailTitle.text = "${item.postType} ${item.description}"

            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            binding.tvDetailDate.text = sdf.format(Date(item.postedAt))
            binding.tvDetailTimeAgo.text = getTimeAgo(item.postedAt)

            binding.tvDetailLocation.text = "At ${item.location}"
            binding.tvDetailName.text = "Posted by: ${item.name}"
            binding.tvDetailPhone.text = "Phone: ${item.phone}"
            binding.tvDetailCategory.text = "Category: ${item.category}"

            if (!item.imagePath.isNullOrEmpty()) {
                binding.ivDetailImage.setImageURI(Uri.parse(item.imagePath))
                binding.ivDetailImage.visibility = View.VISIBLE
            } else {
                binding.ivDetailImage.visibility = View.GONE
            }

            binding.btnRemove.setOnClickListener {
                viewModel.delete(item)
                findNavController().navigateUp()
            }
        }
    }

    private fun getTimeAgo(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val days = diff / (1000 * 60 * 60 * 24)
        val hours = diff / (1000 * 60 * 60)
        val minutes = diff / (1000 * 60)
        return when {
            minutes < 1 -> "Just now"
            hours < 1 -> "$minutes minutes ago"
            days < 1 -> "$hours hours ago"
            else -> "$days days ago"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}