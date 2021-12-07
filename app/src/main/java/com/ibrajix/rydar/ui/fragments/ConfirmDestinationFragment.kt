package com.ibrajix.rydar.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.FragmentConfirmDestinationBinding
import com.ibrajix.rydar.databinding.FragmentHomeBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior

import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.navigation.NavArgs
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.GeoApiContext
import com.ibrajix.rydar.network.EndPoints
import com.google.maps.DirectionsApi

import com.google.maps.DirectionsApiRequest
import com.google.maps.model.DirectionsResult
import com.google.maps.model.DirectionsRoute
import com.google.maps.model.ComponentFilter.route

import com.google.maps.model.DirectionsLeg
import com.google.maps.model.DirectionsStep
import com.google.maps.model.EncodedPolyline
import com.google.android.gms.maps.model.PolylineOptions

import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Exception


class ConfirmDestinationFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentConfirmDestinationBinding? = null
    private val binding get() = _binding!!

    //init map
    private var mMap: GoogleMap? = null

   val args: ConfirmDestinationFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentConfirmDestinationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        handleClicks()

    }


    private fun handleClicks(){

        //on click lyt normal ride
        binding.lytNormalRide.setOnClickListener {

            //change the background
            binding.lytNormalRide.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_grey))

            //others
            binding.lytPremiumRide.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

        //on click lyt premium ride
        binding.lytPremiumRide.setOnClickListener {

            //change the background
            binding.lytPremiumRide.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_grey))

            //others
            binding.lytNormalRide.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap

        //style
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style
            )
        )

        val fromPoint1 = args.direction.fromPoint1?:0.00
        val fromPoint2 = args.direction.fromPoint2?:0.00
        val toPoint1 = args.direction.toPoint1?:0.00
        val toPoint2 = args.direction.toPoint2?:0.00


        val fromLocation = LatLng(fromPoint1, fromPoint2)
        val markerOptions1 = MarkerOptions()
        markerOptions1.position(fromLocation)
        markerOptions1.title(getString(R.string.from))
        markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        mMap?.addMarker(markerOptions1)



        val toLocation = LatLng(toPoint1, toPoint2)
        val markerOptions2 = MarkerOptions()
        markerOptions2.position(toLocation)
        markerOptions2.title(getString(R.string.to))
        markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
        mMap?.addMarker(markerOptions2)

        val zaragoza = LatLng(41.648823, -0.889085)

        //Define list to get all latLng for the route
        val path: MutableList<LatLng> = ArrayList()

        //Execute Directions API request
        val context = GeoApiContext.Builder()
            .apiKey(EndPoints.DIRECTION_API_KEY)
            .build()
        val req = DirectionsApi.getDirections(context, "$fromPoint1,$fromPoint2", "$toPoint1,$toPoint2")
        try {
            val res = req.await()

            //Loop through legs and steps to get encoded polylines of each step
            if (res.routes != null && res.routes.isNotEmpty()) {
                val route = res.routes[0]
                if (route.legs != null) {
                    for (i in route.legs.indices) {
                        val leg = route.legs[i]
                        if (leg.steps != null) {
                            for (j in leg.steps.indices) {
                                val step = leg.steps[j]
                                if (step.steps != null && step.steps.isNotEmpty()) {
                                    for (k in step.steps.indices) {
                                        val step1 = step.steps[k]
                                        val points1 = step1.polyline
                                        if (points1 != null) {
                                            //Decode polyline and add points to list of route coordinates
                                            val coords1 = points1.decodePath()
                                            for (coord1 in coords1) {
                                                path.add(LatLng(coord1.lat, coord1.lng))
                                            }
                                        }
                                    }
                                } else {
                                    val points = step.polyline
                                    if (points != null) {
                                        //Decode polyline and add points to list of route coordinates
                                        val coords = points.decodePath()
                                        for (coord in coords) {
                                            path.add(LatLng(coord.lat, coord.lng))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("exc", ex.localizedMessage?:"")
        }

        //Draw the polyline
        if (path.size > 0) {
            val opts = PolylineOptions().addAll(path).color(Color.BLACK).width(5f)
            mMap?.addPolyline(opts)
        }
        mMap?.uiSettings?.isZoomControlsEnabled = true
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(fromLocation, 6f))
    }

}