package com.ibashkimi.wheel.comments

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.wheel.R

class CommentViewHolder(inflater: LayoutInflater, parent: ViewGroup) : RecyclerView.ViewHolder(
    inflater.inflate(
        R.layout.fragment_comment_list_dialog_item,
        parent,
        false
    )
) {
    val userImage: ImageView = itemView.findViewById(R.id.userImage)
    val name: TextView = itemView.findViewById(R.id.name)
    val content: TextView = itemView.findViewById(R.id.content)
    val time: TextView = itemView.findViewById(R.id.time)
}