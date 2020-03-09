package com.ibashkimi.wheel.messaging.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.databinding.ItemMessageBinding
import com.stfalcon.chatkit.messages.MessageInput
import java.text.SimpleDateFormat


class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels(
        { this },
        { ChatViewModel.Factory(args.chatId) })

    private val args: ChatFragmentArgs by navArgs()

    private lateinit var adapter: MessagesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        val recyclerView = root.findViewById<RecyclerView>(R.id.messagesList)
        val toolbar = root.findViewById<Toolbar>(R.id.toolbar)
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_back_nav)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
            inflateMenu(R.menu.chat)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_clear -> {
                        viewModel.clearChat(args.chatId)
                        true
                    }
                    R.id.action_delete -> {
                        viewModel.deleteChat(args.chatId)
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(context).apply {
            reverseLayout = true
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
        )
        adapter = MessagesAdapter()
        recyclerView.adapter = adapter

        val inputView: MessageInput = root.findViewById(R.id.input)
        inputView.setInputListener {
            viewModel.sendMessage(args.chatId, it.toString())
            true
        }

        viewModel.user.observe(viewLifecycleOwner, Observer {
            toolbar.title = it.displayName
        })

        viewModel.messagesPaged.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        return root
    }

    inner class MessagesAdapter :
        PagedListAdapter<Message, MessagesViewHolder>(messageDiffCallback) {

        /*override fun getItemViewType(position: Int): Int {
            val item: Message = getItem(position) ?:return 0
            return if (item.user) ==
        }*/

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder =
            MessagesViewHolder(
                ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
            getItem(position)?.let {
                val isSameUserAsAbove = if (position - 1 > -1) {
                    getItem(position - 1)?.user?.uid == it.user?.uid
                } else false

                holder.bind(it, isSameUserAsAbove)
            }
        }
    }

    inner class MessagesViewHolder(val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Message, isSameUserAsAbove: Boolean) {
            /*if (isSameUserAsAbove) {
                binding.user.isVisible = false
            } else {
                binding.user.text = item.user.displayName
            }*/
            binding.user.text =
                item.user?.displayName ?: binding.user.context.getString(R.string.no_name)
            binding.content.text = item.content
            binding.time.text = formattedTime(item.created.time)
            binding.root.setOnLongClickListener {
                MaterialAlertDialogBuilder(binding.root.context)
                    .setTitle(R.string.delete_message)
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        viewModel.deleteMessage(item)
                    }
                    .setNegativeButton(android.R.string.cancel) { dialogInterface, _ ->
                        dialogInterface.dismiss()
                    }
                    .show()
                true
            }
        }

        private val formatter = SimpleDateFormat("HH:mm")

        private fun formattedTime(time: Long): String {
            return formatter.format(time)
        }
    }

    companion object {
        val messageDiffCallback = object : DiffUtil.ItemCallback<Message>() {
            override fun areItemsTheSame(oldItem: Message, newItem: Message) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Message, newItem: Message) = oldItem == newItem
        }
    }
}
