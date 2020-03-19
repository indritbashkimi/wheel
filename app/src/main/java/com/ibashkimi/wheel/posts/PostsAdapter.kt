package com.ibashkimi.wheel.posts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.core.model.posts.UserPost
import com.ibashkimi.wheel.core.relativeTimeSpan
import com.ibashkimi.wheel.databinding.ItemPostBinding

class PostsAdapter(var listener: (ClickEvent) -> Unit) :
    PagedListAdapter<UserPost, PostsAdapter.ViewHolder>(postDiffCallback) {

    data class ClickEvent(val post: Post, val isLongClick: Boolean)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(parent.inflater(), parent, false)
        return ViewHolder(binding)
    }

    fun ViewGroup.inflater(): LayoutInflater = LayoutInflater.from(this.context)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        val post = item.post
        val user = item.user
        with(holder) {
            binding.userName.text =
                user.displayName ?: binding.userName.context.getString(R.string.no_name)
            Glide.with(binding.profileImage).load(user.imageUrl)
                .placeholder(R.drawable.ic_profile_pic)
                .into(binding.profileImage)
            if (post.position == null) {
                binding.position.isVisible = false
            } else {
                binding.position.isVisible = true
                binding.position.text =
                    post.position?.address ?: post.position?.run { "$latitude : $longitude" }
            }
            binding.content.text = when (val content = post.content) {
                is Content.Text -> content.text
                else -> "Unsupported content"
            }
            binding.time.text = post.created.relativeTimeSpan()
            binding.root.setOnClickListener {
                listener(
                    ClickEvent(post, false)
                )
            }
            binding.root.setOnLongClickListener {
                listener(
                    ClickEvent(post, true)
                )
                true
            }
            if (post.position == null) {
                binding.position.text = null
                binding.position.isVisible = false
            } else {
                binding.position.text = post.position?.run { address }
                    ?: "${post.position?.latitude}: ${post.position?.longitude}"
            }
            android.util.Log.d("PostsAdapter", "liked? ${post.liked}")
            binding.like.setImageResource(if (post.liked) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        }
    }

    class ViewHolder(val binding: ItemPostBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        val postDiffCallback = object : DiffUtil.ItemCallback<UserPost>() {
            override fun areItemsTheSame(oldItem: UserPost, newItem: UserPost): Boolean {
                return oldItem.post.uid == newItem.post.uid
            }

            override fun areContentsTheSame(oldItem: UserPost, newItem: UserPost): Boolean {
                return oldItem.post == newItem.post
            }
        }
    }
}