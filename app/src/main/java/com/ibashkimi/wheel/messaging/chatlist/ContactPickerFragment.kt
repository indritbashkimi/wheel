package com.ibashkimi.wheel.messaging.chatlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.adapter.PagedUserAdapter
import com.ibashkimi.wheel.core.toast

class ContactPickerFragment : Fragment() {

    private val viewModel: ContactPickerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_contact_picker, container, false)

        root.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.pick_contact)
            setNavigationIcon(R.drawable.ic_back_nav)
            setNavigationOnClickListener {
                findNavController().navigateUp()
            }
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )

        val adapter = PagedUserAdapter {
            viewModel.createChat(it)
        }
        recyclerView.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner, Observer {
            if (it != null) adapter.submitList(it) else toast("Cannot load data")
        })
        viewModel.selectedChat.observe(viewLifecycleOwner, Observer {
            findNavController().navigate(
                ContactPickerFragmentDirections.actionContactPickerToChat(it)
            )
        })

        return root
    }
}