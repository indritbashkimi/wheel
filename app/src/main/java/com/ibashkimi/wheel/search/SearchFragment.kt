package com.ibashkimi.wheel.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.adapter.PagedUserAdapter
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.home.HomeFragmentDirections

class SearchFragment : Fragment() {
    private val viewModel: SearchViewModel by viewModels()
    private lateinit var adapter: PagedUserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search, container, false)

        root.findViewById<Toolbar>(R.id.toolbar).apply {
            setTitle(R.string.search_title)
        }

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        adapter = PagedUserAdapter {
            requireActivity().findNavController(R.id.main_nav_host_fragment)
                .navigate(HomeFragmentDirections.actionHomeToProfile(it.uid))
        }
        recyclerView.adapter = adapter

        viewModel.users.observe(viewLifecycleOwner, Observer { users ->
            users?.let { adapter.submitList(it) } ?: toast("load error")
        })

        return root
    }
}