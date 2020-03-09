package com.ibashkimi.wheel.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.User

class PagedUserAdapter(private val listener: (User) -> Unit) :
    PagedListAdapter<User, UserViewHolder>(userDiffCallback) {

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position) ?: return, listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onViewRecycled(holder: UserViewHolder) {
        holder.clear()
    }
}

val userDiffCallback = object : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.uid == newItem.uid

    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
}