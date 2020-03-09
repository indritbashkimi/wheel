package com.ibashkimi.wheel.messaging.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.core.model.messaging.Room
import com.ibashkimi.wheel.core.relativeTimeSpan
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.firestore.messaging.FirestoreMessagingDataManager
import com.ibashkimi.wheel.home.HomeFragmentDirections


class ChatListFragment : Fragment() {

    private val viewModel: ChatListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_messages, container, false)

        root.findViewById<View>(R.id.fab).setOnClickListener { pickContact() }

        root.findViewById<Toolbar>(R.id.toolbar).apply {
            title = getString(R.string.title_messages)
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        val adapter = ChatsAdapter()
        recyclerView.adapter = adapter

        viewModel.chats.observe(viewLifecycleOwner, Observer {
            if (it == null) {
                //toast("Cannot load chats")
                root.findViewById<View>(R.id.no_messages).visibility = View.VISIBLE
            } else {
                root.findViewById<View>(R.id.no_messages).visibility = View.INVISIBLE
                adapter.submitList(it)
            }
        })

        return root
    }

    private fun pickContact() {
        requireActivity().findNavController(R.id.main_nav_host_fragment)
            .navigate(
                HomeFragmentDirections.actionHomeToContactPicker(
                    FirestoreUserManager().currentUserId!!
                )
            )
    }

    private fun openChat(chat: Room) {
        requireActivity().findNavController(R.id.main_nav_host_fragment)
            .navigate(HomeFragmentDirections.actionHomeToChat(chat.uid))
    }


    inner class ChatsAdapter : PagedListAdapter<Room, ChatsViewHolder>(coinDiffCallback) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatsViewHolder =
            ChatsViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_message_dialog,
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ChatsViewHolder, position: Int) {
            getItem(position)?.let {
                holder.bind(it)
            }
        }

        override fun onViewRecycled(holder: ChatsViewHolder) {
            super.onViewRecycled(holder)
            holder.clear()
        }
    }

    // todo view binding
    inner class ChatsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val title: TextView = itemView.findViewById(R.id.name)
        val date: TextView = itemView.findViewById(R.id.date)
        val lastMessage: TextView = itemView.findViewById(R.id.last_message)

        var liveData: LiveData<Message?>? = null // todo no live data here

        fun bind(chat: Room) {
            val participant =
                chat.userParticipants?.firstOrNull { it.uid != FirestoreUserManager().currentUserId!! }
                    ?: return
            Glide.with(icon).load(participant.imageUrl).placeholder(R.drawable.ic_profile_pic)
                .into(icon)
            title.text = participant.displayName ?: title.context.getString(R.string.no_name)
            itemView.setOnClickListener {
                openChat(chat)
            }
            liveData?.removeObservers(viewLifecycleOwner)
            liveData = FirestoreMessagingDataManager().getLastMessage(chat.uid).asLiveData().apply {
                observe(viewLifecycleOwner, Observer { message ->
                    android.util.Log.d("ChatListFragment", "last message: $message")
                    message?.let {
                        date.text = it.created.relativeTimeSpan()
                        lastMessage.text = it.content
                    }
                })
            }
        }

        fun clear() {
            liveData?.removeObservers(viewLifecycleOwner)
        }
    }

    companion object {
        val coinDiffCallback = object : DiffUtil.ItemCallback<Room>() {
            override fun areItemsTheSame(oldItem: Room, newItem: Room) = oldItem.uid == newItem.uid

            override fun areContentsTheSame(oldItem: Room, newItem: Room) = oldItem == newItem
        }
    }
}
