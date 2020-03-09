package com.ibashkimi.wheel.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.User
import com.ibashkimi.wheel.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private val args: EditProfileFragmentArgs by navArgs()

    private val viewModel: EditProfileViewModel by viewModels(
        { this },
        { EditProfileViewModel.Factory(args.userId) }
    )

    private var _binding: FragmentEditProfileBinding? = null
    private val binding: FragmentEditProfileBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)

        binding.apply {
            root.findViewById<Toolbar>(R.id.toolbar).apply {
                setTitle(R.string.edit_profile)
                setNavigationIcon(R.drawable.ic_back_nav)
                setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }
            viewModel.user.observe(viewLifecycleOwner, Observer { user ->
                if (user == null) {
                    onUserLoadError()
                } else {
                    onUserReady(user)
                }
            })
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onUserReady(user: User) {
        binding.apply {
            error.isVisible = false
            dataLayout.isVisible = true
            name.setText(user.displayName)
            email.setText(user.email)
        }
    }

    private fun onUserLoadError() {
        binding.apply {
            error.isVisible = true
            dataLayout.isVisible = false
        }
    }
}