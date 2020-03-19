package com.ibashkimi.wheel.messaging.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
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
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.core.Content
import com.ibashkimi.wheel.core.model.messaging.Message
import com.ibashkimi.wheel.widget.TextMediaInputEditText
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
        adapter = MessagesAdapter(viewModel.userId)
        recyclerView.adapter = adapter

        val inputLayout: TextInputLayout = root.findViewById(R.id.input_layout)
        val inputView: TextMediaInputEditText = root.findViewById(R.id.input)
        inputLayout.setEndIconOnClickListener {
            viewModel.sendMessage(args.chatId, Content.Text(inputView.text.toString()))
            inputView.setText("")
        }
        inputLayout.isEndIconVisible = false
        inputView.addTextChangedListener {
            inputLayout.isEndIconVisible = it?.isNotBlank() ?: false
        }
        inputView.inputContentInfoLiveData.observe(
            viewLifecycleOwner,
            Observer { inputContentInfo ->
                val uri = inputContentInfo.contentUri.toString()
                val content: Content? = when {
                    uri.endsWith(".png") or uri.endsWith(".jpg") -> {
                        Content.Media.Image(inputContentInfo.linkUri.toString(), null)
                    }
                    uri.endsWith(".gif") -> {
                        Content.Media.Animation(inputContentInfo.linkUri.toString(), null)
                    }
                    uri.endsWith(".mp4") or uri.endsWith(".webm") -> {
                        Content.Media.Video(inputContentInfo.linkUri.toString(), null)
                    }
                    else -> null
                }
                content?.let {
                    viewModel.sendMessage(args.chatId, content)
                }
                inputContentInfo.releasePermission()
            })

        viewModel.otherUser.observe(viewLifecycleOwner, Observer {
            toolbar.title = it.displayName
        })
        viewModel.messagesPaged.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        return root
    }

    inner class MessagesAdapter(val userId: String) :
        PagedListAdapter<Message, MessagesViewHolder>(messageDiffCallback) {

        override fun getItemViewType(position: Int): Int {
            return if (getItem(position)?.userId == userId) R.layout.item_message_user else R.layout.item_message_other
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {
            return MessagesViewHolder(
                LayoutInflater.from(parent.context).inflate(viewType, parent, false)
            )
        }

        override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {
            getItem(position)?.let {
                val isSameUserAsAbove = if (position + 1 < itemCount) {
                    getItem(position + 1)?.user?.uid == it.user?.uid
                } else false

                holder.bind(it, isSameUserAsAbove)
            }
        }

        override fun onViewRecycled(holder: MessagesViewHolder) {
            super.onViewRecycled(holder)
            holder.recycle()
        }
    }

    inner class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val user = itemView.findViewById<TextView>(R.id.user)
        private val contentText = itemView.findViewById<TextView>(R.id.content)
        private val time = itemView.findViewById<TextView>(R.id.time)
        private val image = itemView.findViewById<ImageView>(R.id.imageView)

        fun bind(item: Message, isSameUserAsAbove: Boolean) {
            user.isVisible = !isSameUserAsAbove
            user.text = item.user?.displayName ?: user.context.getString(R.string.no_name)
            image.isVisible = false
            when (val content = item.content) {
                is Content.Text -> {
                    contentText.text = content.text
                }
                is Content.Media -> {
                    image.isVisible = true
                    contentText.text = content.text
                    Glide.with(requireContext()).load(content.uri).centerCrop().into(image)
                }
                else -> contentText.text = "Unsupported content"
            }

            time.text = formattedTime(item.created.time)
            itemView.setOnLongClickListener {
                MaterialAlertDialogBuilder(itemView.context)
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

        fun recycle() {
            image.visibility = View.GONE
            user.text = null
            contentText.text = null
            time.text = null
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

            override fun areContentsTheSame(oldItem: Message, newItem: Message) =
                oldItem.content == newItem.content
        }
    }
}
