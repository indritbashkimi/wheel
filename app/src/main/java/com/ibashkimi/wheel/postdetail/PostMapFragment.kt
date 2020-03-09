package com.ibashkimi.wheel.postdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ibashkimi.wheel.PreferenceHelper
import com.ibashkimi.wheel.R
import com.ibashkimi.wheel.databinding.PostMapFragmentBinding
import com.ibashkimi.wheel.location.MapUtils
import com.ibashkimi.wheel.location.setUpStyle

class PostMapFragment : Fragment(), OnMapReadyCallback {

    private val args: PostMapFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = PostMapFragmentBinding.inflate(inflater, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.post_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setUpStyle(requireContext(), PreferenceHelper(requireContext()).mapStyle)
        val position = LatLng(args.latitude.toDouble(), args.longitude.toDouble())
        googleMap.addMarker(MarkerOptions().position(position))
        val bounds = MapUtils.calculateBounds(position, 100.0)
        val margin = resources.getDimensionPixelOffset(R.dimen.place_picker_circle_margin)
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, margin)
        // Show the current location in Google Map
        googleMap.moveCamera(cameraUpdate)
    }
}