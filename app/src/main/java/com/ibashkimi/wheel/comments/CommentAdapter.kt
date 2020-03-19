package com.ibashkimi.wheel.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.relativeTimeSpan
import com.ibashkimi.wheel.utils.AdapterItemClickListener


class CommentAdapter(var listener: AdapterItemClickListener<Comment>? = null) :
    PagedListAdapter<Comment, CommentViewHolder>(commentDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        return CommentViewHolder(LayoutInflater.from(parent.context), parent)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position) ?: return
        val user = comment.user
        Glide.with(holder.userImage).load(user?.imageUrl).placeholder(R.drawable.ic_profile_pic)
            .into(holder.userImage)
        holder.name.text = user?.displayName ?: holder.name.context.getString(R.string.no_name)
        holder.content.text = when (val content = comment.content) {
            is Content.Text -> content.text
            else -> "Unsupported content"
        }
        holder.time.text = comment.createdAt.relativeTimeSpan()
        holder.itemView.setOnClickListener {
            listener?.onItemClick(getItem(holder.adapterPosition), false)
        }
        holder.itemView.setOnLongClickListener {
            listener?.onItemClick(getItem(holder.adapterPosition), true)
            true
        }
    }

    companion object {
        val commentDiffCallback = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment) =
                oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
        }
    }
}

