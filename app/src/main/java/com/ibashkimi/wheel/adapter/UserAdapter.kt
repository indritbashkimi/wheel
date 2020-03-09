package com.ibashkimi.wheel.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.User

class UsersAdapter(val data: ArrayList<User> = ArrayList(), private val listener: (User) -> Unit) :
    RecyclerView.Adapter<UserViewHolder>() {

    fun onNewData(newData: List<User>) {
        data.clear()
        data.addAll(newData)
        this.notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(data[position], listener)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return UserViewHolder(itemView)
    }

    override fun onViewRecycled(holder: UserViewHolder) {
        holder.clear()
    }
}
