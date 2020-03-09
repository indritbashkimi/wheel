package com.ibashkimi.wheel.profile

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.databinding.FragmentProfileBinding
import com.ibashkimi.wheel.firestore.core.FirestoreUserManager
import com.ibashkimi.wheel.home.HomeFragmentDirections
import com.ibashkimi.wheel.posts.PostsAdapter
import kotlin.math.abs


class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by viewModels(
        { this },
        { ProfileViewModel.Factory(userId) }
    )

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!

    private val userId: String by lazy {
        arguments?.getString("userId") ?: FirestoreUserManager().currentUserId!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        binding.apply {
            toolbar.visibility = View.INVISIBLE
            if (userId != FirestoreUserManager().currentUserId) {
                toolbar.setNavigationIcon(R.drawable.ic_back_nav)
                toolbar.setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }
            appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
                if (abs(verticalOffset) - appBarLayout.totalScrollRange == 0) {
                    // Collapsed
                    toolbar.visibility = View.VISIBLE
                } else {
                    // Expanded
                    toolbar.visibility = View.INVISIBLE
                }
            })
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            recyclerView.layoutManager = layoutManager
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    layoutManager.orientation
                )
            )
            val adapter = PostsAdapter {
                if (it.isLongClick) {
                    toast("Long click does nothing yet")
                } else {
                    Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)
                        .navigate(HomeFragmentDirections.actionHomeToPostDetails(it.post.uid))
                }
            }
            recyclerView.adapter = adapter

            viewModel.user.observe(viewLifecycleOwner, Observer { user ->
                user?.let { showUser(it) } ?: showLoadingError()
            })
            viewModel.postsPaged.observe(viewLifecycleOwner, Observer { posts ->
                Log.d("ProfileFragment", "posts: $posts")
                posts?.let { adapter.submitList(it) }
            })

            if (FirestoreUserManager().currentUserId != userId) {
                val clickListener: View.OnClickListener = View.OnClickListener {
                    actionButton.isEnabled = false
                    viewModel.createConnection(userId)
                }
                actionButton.isEnabled = false
                actionButton.setOnClickListener(clickListener)
                viewModel.connection.observe(viewLifecycleOwner, Observer { connection ->
                    if (connection != null) {
                        actionButton.isEnabled = false
                        if (connection.state == "pending") {
                            actionButton.setText(R.string.following_pending)
                        } else {
                            actionButton.setText(R.string.following)
                        }
                    } else {
                        actionButton.isEnabled = true
                        actionButton.setText(R.string.follow)
                    }

                })
            } else {
                actionButton.isEnabled = true
                actionButton.setText(R.string.edit_profile)
                actionButton.setOnClickListener {
                    navController.navigate(HomeFragmentDirections.actionHomeToEditProfile(userId))
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val navController: NavController
        get() = Navigation.findNavController(requireActivity(), R.id.main_nav_host_fragment)

    private fun showUser(user: User) {
        Log.d("ProfileFragment", "showUser $user")
        binding.apply {
            toolbar.title = user.displayName ?: getString(R.string.no_name)
            Glide.with(profileImage).load(user.imageUrl).placeholder(R.drawable.ic_profile_pic)
                .into(profileImage)
            name.text = user.displayName
        }
    }

    private fun showLoadingError() {
        toast("Cannot load user")
    }
}
