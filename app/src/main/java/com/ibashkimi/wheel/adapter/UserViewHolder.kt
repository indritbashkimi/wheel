package com.ibashkimi.wheel.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.User

class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val icon: ImageView = itemView.findViewById(R.id.icon)
    val name: TextView = itemView.findViewById(R.id.name)

    fun bind(user: User, listener: (User) -> Unit) {
        itemView.setOnClickListener {
            listener(user)
        }
        Glide.with(icon).load(user.imageUrl).placeholder(R.drawable.ic_profile_pic).into(icon)
        name.text = user.displayName ?: itemView.context.getString(R.string.no_name)
    }

    fun clear() {
        icon.setImageDrawable(null)
        name.text = null
    }
}