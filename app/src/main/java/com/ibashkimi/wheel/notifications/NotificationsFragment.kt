package com.ibashkimi.wheel.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.core.Event
import com.ibashkimi.wheel.core.relativeTimeSpan
import com.ibashkimi.wheel.databinding.FragmentNotificationsBinding
import com.ibashkimi.wheel.home.HomeFragmentDirections

class NotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private var _binding: FragmentNotificationsBinding? = null
    private val binding: FragmentNotificationsBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentNotificationsBinding.inflate(inflater, container, false).run {
            _binding = this

            root.findViewById<Toolbar>(R.id.toolbar).apply {
                title = getString(R.string.title_notifications)
            }
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL
                )
            )
            val adapter = PagedEventAdapter {

            }
            recyclerView.adapter = adapter

            viewModel.events.observe(viewLifecycleOwner, Observer {
                binding.recyclerView.visibility = View.VISIBLE
                binding.doneImage.visibility = View.GONE
                adapter.submitList(it)
            })

            root
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class PagedEventAdapter(private val listener: (Event) -> Unit) :
        PagedListAdapter<Event, BasicViewHolder>(eventDiffCallback) {

        override fun getItemViewType(position: Int): Int {
            return when (getItem(position) ?: return -1) {
                is Event.NewFollowRequestEvent -> 1
                is Event.FollowAcceptedEvent -> 2
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BasicViewHolder {
            return when (viewType) {
                1 -> RequestViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notif_request,
                        parent,
                        false
                    )
                )
                2 -> RequestAcceptedViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_notif_request,
                        parent,
                        false
                    )
                )
                else -> throw IllegalArgumentException()
            }
        }

        override fun onBindViewHolder(holder: BasicViewHolder, position: Int) {
            holder.bind(getItem(position) ?: return)
        }

        override fun onViewRecycled(holder: BasicViewHolder) {
            holder.clear()
        }
    }

    abstract class BasicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context: Context = itemView.context

        abstract fun bind(event: Event)

        abstract fun clear()
    }

    inner class RequestViewHolder(itemView: View) : BasicViewHolder(itemView) {
        private val type: TextView = itemView.findViewById(R.id.type)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val action: Button = itemView.findViewById(R.id.actionButton)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val cover: View = itemView.findViewById(R.id.cover)

        override fun bind(event: Event) {
            val request = event as Event.NewFollowRequestEvent
            type.setText(R.string.follow_request)
            itemView.setOnClickListener {
                viewModel.markAsDone(event)
                Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                    .navigate(HomeFragmentDirections.actionHomeToProfile(request.fromUserId))
            }
            itemView.setOnLongClickListener {
                true
            }
            cover.visibility = if (request.done) View.GONE else View.VISIBLE
            time.text = event.created.relativeTimeSpan()

            Glide.with(icon).load(request.fromUser?.imageUrl).placeholder(R.drawable.ic_profile_pic)
                .into(icon)

            name.text = request.fromUser?.displayName ?: name.context.getString(R.string.no_name)

            val connection = request.connection
            Log.d(TAG, "connection: $connection")
            if (connection != null) {
                action.visibility = View.VISIBLE
                action.setText(if (connection.state!! == "pending") R.string.accept_request else R.string.request_accepted)
                action.isEnabled = connection.state != "confirmed"
                action.setOnClickListener {
                    viewModel.markAsDone(event)
                    action.isEnabled = false
                    viewModel.acceptRequest(connection)
                }
            } else {
                action.visibility = View.INVISIBLE
            }
        }

        override fun clear() {

        }
    }

    inner class RequestAcceptedViewHolder(itemView: View) : BasicViewHolder(itemView) {
        private val type: TextView = itemView.findViewById(R.id.type)
        private val icon: ImageView = itemView.findViewById(R.id.icon)
        private val name: TextView = itemView.findViewById(R.id.name)
        private val action: Button = itemView.findViewById(R.id.actionButton)
        private val time: TextView = itemView.findViewById(R.id.time)
        private val cover: View = itemView.findViewById(R.id.cover)

        override fun bind(event: Event) {
            val request = event as Event.FollowAcceptedEvent
            type.setText(R.string.follow_request_accepted)
            action.visibility = View.GONE
            itemView.setOnClickListener {
                viewModel.markAsDone(event)
                Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                    .navigate(HomeFragmentDirections.actionHomeToProfile(request.fromUserId))
            }
            itemView.setOnLongClickListener {
                true
            }
            cover.visibility = if (request.done) View.GONE else View.VISIBLE
            time.text = event.created.relativeTimeSpan()

            request.fromUser?.imageUrl?.let {
                Glide.with(icon).load(it).into(icon)
            }

            name.text = request.fromUser?.displayName

            val connection = request.connection
            Log.d(TAG, "connection: $connection")
        }

        override fun clear() {

        }
    }

    val eventDiffCallback = object : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.uid == newItem.uid

        // todo
        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        val TAG = NotificationsFragment::class.java.simpleName
    }
}
