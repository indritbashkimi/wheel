package com.ibashkimi.wheel.location

import android.content.Context
import android.content.res.Resources
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.SphericalUtil


object MapUtils {

    fun calculateBounds(center: LatLng, radius: Double): LatLngBounds {
        return LatLngBounds.Builder().include(SphericalUtil.computeOffset(center, radius, 0.0))
            .include(SphericalUtil.computeOffset(center, radius, 90.0))
            .include(SphericalUtil.computeOffset(center, radius, 180.0))
            .include(SphericalUtil.computeOffset(center, radius, 270.0)).build()
    }

    fun resolveMapStyle(mapStyle: Int): Int {
        when (mapStyle) {
            0 -> return GoogleMap.MAP_TYPE_NORMAL
            1 -> return GoogleMap.MAP_TYPE_SATELLITE
            2 -> return GoogleMap.MAP_TYPE_HYBRID
            3 -> return GoogleMap.MAP_TYPE_TERRAIN
            else -> return GoogleMap.MAP_TYPE_HYBRID
        }
    }
}

fun setUpMapStyle(context: Context, googleMap: GoogleMap, style: String): Boolean {
    var mapStyleRes = -1
    when (style) {
        "normal" -> googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        "hybrid" -> googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID
        "satellite" -> googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        "terrain" -> googleMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
        "aubergine" -> mapStyleRes = R.raw.style_aubergine
        "dark" -> mapStyleRes = R.raw.style_dark
        "night" -> mapStyleRes = R.raw.style_night
        "retro" -> mapStyleRes = R.raw.style_retro
        "silver" -> mapStyleRes = R.raw.style_silver
    }
    if (mapStyleRes != -1) {
        return try {
            // Customise the styling of the base mapToDomain using a JSON object defined
            // in a raw resource file.
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, mapStyleRes))
        } catch (e: Resources.NotFoundException) {
            false
        }
    }
    return false
}

fun GoogleMap.setUpStyle(context: Context, mapStyle: String) {
    setUpMapStyle(
        context,
        this,
        mapStyle
    )
}
