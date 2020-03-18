package com.ibashkimi.wheel.postdetail

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.PagedList
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.comments.CommentAdapter
import com.ibashkimi.wheel.core.model.posts.Comment
import com.ibashkimi.wheel.core.model.posts.Content
import com.ibashkimi.wheel.core.model.posts.Post
import com.ibashkimi.wheel.core.relativeTimeSpan
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.databinding.FragmentPostDetailBinding
import com.ibashkimi.wheel.utils.AdapterItemClickListener


class PostDetailFragment : Fragment() {

    private var _binding: FragmentPostDetailBinding? = null

    private val binding: FragmentPostDetailBinding get() = _binding!!

    private val viewModel: PostDetailViewModel by viewModels(
        { this },
        { PostDetailViewModel.Factory(args.postId) })

    private val args: PostDetailFragmentArgs by navArgs()

    private lateinit var commentAdapter: CommentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPostDetailBinding.inflate(inflater, container, false)

        setupToolbar()
        setupComments()

        binding.like.setOnClickListener {
            viewModel.likePost()
        }

        viewModel.post.observe(viewLifecycleOwner, Observer { post ->
            post?.let { onPostReady(it) } ?: onPostNotAvailable()
        })

        viewModel.user.observe(viewLifecycleOwner, Observer {
            it?.let { user ->
                binding.userName.text = user.displayName ?: getString(R.string.no_name)
                Glide.with(binding.profileImage).load(user.imageUrl)
                    .placeholder(R.drawable.ic_profile_pic).into(binding.profileImage)
                binding.userLayout.setOnClickListener {
                    findNavController().navigate(
                        PostDetailFragmentDirections.actionPostToProfile(user.uid)
                    )
                }
            }
        })

        viewModel.comments.observe(viewLifecycleOwner, Observer { comments ->
            comments?.let { onCommentsReady(it) } ?: onCommentsNotAvailable()
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupToolbar() {
        binding.toolbar.apply {
            setTitle(R.string.title_post_detail)
            setNavigationIcon(R.drawable.ic_back_nav)
            setNavigationOnClickListener { findNavController().navigateUp() }
            inflateMenu(R.menu.menu_post_detail)
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.action_delete -> {
                        val post = viewModel.post.value
                        if (post != null) {
                            viewModel.deletePost(post)
                            findNavController().navigateUp()
                        } else
                            Toast.makeText(
                                requireContext(), "Post is null",
                                Toast.LENGTH_SHORT
                            ).show()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun setupComments() {
        val layoutManager = LinearLayoutManager(binding.commentsRecyclerView.context)
        binding.commentsRecyclerView.layoutManager = layoutManager
        val dividerItemDecoration =
            DividerItemDecoration(binding.commentsRecyclerView.context, layoutManager.orientation)
        binding.commentsRecyclerView.addItemDecoration(dividerItemDecoration)
        commentAdapter = CommentAdapter()
        commentAdapter.listener =
            AdapterItemClickListener { comment, isLongClick ->
                showCommentOptionsDialog(comment)
            }

        binding.commentsRecyclerView.adapter = commentAdapter
        binding.commentsRecyclerView.isNestedScrollingEnabled = false

        binding.inputLayout.setEndIconOnClickListener {
            val comment = Comment(
                uid = "",
                content = Content(
                    binding.input.text.toString(),
                    null
                ),
                createdAt = System.currentTimeMillis(),
                postId = args.postId,
                userId = FirebaseAuth.getInstance().currentUser!!.uid
            )

            Log.d("PostDetailFragment", "Saving comment $comment.")
            viewModel.saveComment(comment)
            binding.input.setText("")
        }
        binding.inputLayout.isEndIconVisible = false
        binding.input.addTextChangedListener {
            binding.inputLayout.isEndIconVisible = it?.isNotBlank() ?: false
        }
    }

    private fun onPostReady(post: Post) {
        binding.content.text = post.content.textContent
        binding.time.text = post.created.relativeTimeSpan()
        val position = post.position
        if (position == null) {
            binding.position.isVisible = false
        } else {
            binding.position.isVisible = true
            binding.position.text =
                position.address ?: "${position.latitude}, ${position.longitude
                }"
            binding.position.setOnClickListener {
                findNavController().navigate(
                    PostDetailFragmentDirections.actionPostToMap(
                        position.latitude.toString(),
                        position.longitude.toString()
                    )
                )
            }
        }
    }

    private fun onPostNotAvailable() {
        Toast.makeText(requireContext(), "Cannot load post.", Toast.LENGTH_SHORT).show()
    }

    private fun onCommentsReady(comments: PagedList<Comment>) {
        commentAdapter.submitList(comments)
        /*if (comments.size < 1) {
            binding.commentsRecyclerView.visibility = View.GONE
            noComments.visibility = View.VISIBLE
        } else {
            binding.commentsRecyclerView.visibility = View.VISIBLE
            noComments.visibility = View.GONE
        }*/
    }

    private fun onCommentsNotAvailable() {
        toast("Cannot load comments.")
    }

    private fun showCommentOptionsDialog(comment: Comment) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.comment_options)
            .setItems(R.array.comment_options_array) { dialog, which ->
                when (which) {
                    1 -> {
                        viewModel.deleteComment(comment)
                    }
                    else -> toast("Not implemented yet")
                }
                dialog.dismiss()
            }
            .show()
    }
}
