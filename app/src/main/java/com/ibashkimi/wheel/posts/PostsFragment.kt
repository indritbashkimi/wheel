package com.ibashkimi.wheel.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibashkimi.wheel.AuthViewModel
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.databinding.FragmentPostsBinding
import com.ibashkimi.wheel.firebase.auth.firebaseLogOut
import com.ibashkimi.wheel.home.HomeFragmentDirections

class PostsFragment : Fragment() {

    private var _binding: FragmentPostsBinding? = null
    private val binding: FragmentPostsBinding get() = _binding!!
    private lateinit var postsAdapter: PostsAdapter

    private val viewModel: PostsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostsBinding.inflate(inflater, container, false)

        val toolbar = binding.toolbar
        toolbar.inflateMenu(R.menu.menu_posts)
        toolbar.title = getString(R.string.app_name)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_sign_out) {
                // todo
                firebaseLogOut(requireContext())
                ViewModelProvider(this).get(AuthViewModel::class.java).userLiveData.value = null
                true
            } else {
                val nav =
                    Navigation.findNavController(requireActivity().findViewById(R.id.main_nav_host_fragment))
                NavigationUI.onNavDestinationSelected(it, nav)
            }
        }

        binding.fab.setImageResource(R.drawable.ic_add)
        binding.fab.setOnClickListener {
            requireActivity().findNavController(R.id.main_nav_host_fragment)
                .navigate(HomeFragmentDirections.actionHomeToAddPost())
        }

        binding.progressBar.hide()

        postsAdapter = PostsAdapter() {
            if (it.isLongClick) {
                Toast.makeText(context, "Long click does nothing yet", Toast.LENGTH_SHORT).show()
            } else {
                Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                    .navigate(HomeFragmentDirections.actionHomeToPostDetails(it.post.uid!!))
            }
        }
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.addItemDecoration(
            DividerItemDecoration(
                binding.recyclerView.context,
                layoutManager.orientation
            )
        )
        binding.recyclerView.adapter = postsAdapter

        viewModel.postsPaged.observe(viewLifecycleOwner, Observer {
            hideLoading()
            postsAdapter.submitList(it)
        })

        showLoading()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showLoading() {
        binding.progressBar.show()
        binding.recyclerView.isVisible = false
    }

    private fun hideLoading() {
        binding.progressBar.hide()
        binding.recyclerView.isVisible = true
    }

    private fun showLoadingError() {
        toast("Loading error")
    }
}
