package com.ibashkimi.wheel.addpost

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.ibashkimi.wheel.PreferenceHelper
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.core.model.posts.Position
import com.ibashkimi.wheel.core.toast
import com.ibashkimi.wheel.databinding.FragmentAddPostBinding
import com.ibashkimi.wheel.location.PlacePickerActivity


class AddPostFragment : Fragment() {

    private val viewModel: AddPostViewModel by viewModels()

    private var _binding: FragmentAddPostBinding? = null

    private val binding: FragmentAddPostBinding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentAddPostBinding.inflate(inflater, container, false).run {
            _binding = this
            root
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.apply {

            root.findViewById<Toolbar>(R.id.toolbar).apply {
                setTitle(R.string.title_add_post)
                setNavigationIcon(R.drawable.ic_clear)
                setNavigationOnClickListener {
                    findNavController().navigateUp()
                }
            }

            if (savedInstanceState == null) {
                postContentInput.setText(viewModel.text.value)
            }
            postContentInput.addTextChangedListener {
                viewModel.text.value = it?.toString()
            }

            bottomAppBar.apply {
                inflateMenu(R.menu.add_post)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.action_location -> {
                            startActivityForResult(
                                Intent(
                                    requireContext(),
                                    PlacePickerActivity::class.java
                                ).putExtra(
                                    "map_style",
                                    PreferenceHelper(requireContext()).mapStyle
                                ), SELECT_PLACE_REQUEST
                            )
                            true
                        }
                        R.id.action_image -> {
                            toast("Not implemented yet")
                            true
                        }
                        R.id.action_camera -> {
                            toast("Not implemented yet")
                            true
                        }
                        else -> false
                    }
                }
            }

            savePost.setOnClickListener {
                if (!viewModel.isValidForSave()) {
                    toast("Please write something")
                } else {
                    createPost()
                }
            }

            viewModel.position.observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    locationLayout.isVisible = false
                    location.text = null
                } else {
                    locationLayout.isVisible = true
                    location.text = it.toString()
                }
            })

            viewModel.image.observe(viewLifecycleOwner, Observer {
                if (it == null) {
                    image.setImageDrawable(null)
                } else {
                    //binding.image.setImage todo
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createPost() {
        viewModel.savePost()
        findNavController().navigateUp()
    }

    private fun addImage() {
        val intent = Intent()
        // Show only images, no videos or anything else
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            // TODO
            toast("Image upload not implemented yet!")
            /*
            val selectedImageUri = data!!.data!!
            val reference = FirebaseStorage.getInstance().reference.child("images")
                .child(selectedImageUri.lastPathSegment!!)
            reference.putFile(selectedImageUri).addOnSuccessListener {
                Toast.makeText(context, "Image uploaded!", Toast.LENGTH_LONG).show()
                //Uri downloadUrl = taskSnapshot.getDownloadUrl();
            }*/
        } else if (requestCode == SELECT_PLACE_REQUEST && resultCode == RESULT_OK) {
            data?.extras?.apply {
                if (containsKey("latitude") && containsKey("longitude")) {
                    val latitude = getDouble("latitude")
                    val longitude = getDouble("longitude")
                    val address = getString("address")
                    viewModel.position.value = Position(latitude, longitude, address)
                }
            }
        }
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
        private const val SELECT_PLACE_REQUEST = 2
    }
}
