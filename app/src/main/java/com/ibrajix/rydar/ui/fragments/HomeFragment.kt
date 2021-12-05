package com.ibrajix.rydar.ui.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.FragmentHomeBinding
import com.ibrajix.rydar.utils.Constants
import com.ibrajix.rydar.utils.GeneralUtility
import permissions.dispatcher.*
import android.location.Geocoder
import android.util.Log
import java.io.IOException
import java.util.*


@RuntimePermissions
class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var geocoder: Geocoder? = null
    var addresses: List<Address>? = null

    //map variables
    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private var lastKnownLocation: Location? = null
    internal var currentLocationMarker: Marker? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val resolutionForResult =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { activityResult ->
            if (activityResult.resultCode == RESULT_OK){
                //user clicked OK, you can startUpdatingLocation(...)
                checkIfPermissionGranted()
            }
            else {
                Toast.makeText(requireContext(), getString(R.string.we_cant_get_your_location), Toast.LENGTH_LONG).show()
            }
        }


    //location call back
    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val locationList = locationResult?.locations
            if (locationList != null) {
                if (locationList.isNotEmpty()){
                    //the last location is the most recent or newest
                    val location = locationList.last()
                    lastKnownLocation = location
                    if (currentLocationMarker != null){
                        currentLocationMarker?.remove()
                    }

                    //place the current location marker here
                    val latLng = LatLng(location.latitude, location.longitude)
                    val markerOptions = MarkerOptions()
                    markerOptions.position(latLng)
                    markerOptions.title(getString(R.string.current_position))
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    currentLocationMarker = mMap.addMarker(markerOptions)

                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0F))

                    //get location text value with geocoder
                    try {
                        addresses = geocoder?.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.get(0)?.getAddressLine(0)
                    }
                    catch (e: IOException){
                        Log.e("lce", e.toString())
                    }

                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //init map stuff and also permission
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())

        geocoder = Geocoder(requireContext(), Locale.getDefault())


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        handleClicks()

        checkIfLocationIsTurnedOn()

    }


    override fun onPause() {
        super.onPause()
        //stop location updates when activity is no longer active
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        checkIfLocationIsTurnedOn()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {

        //init map items
        mMap = googleMap
        locationRequest = LocationRequest()
        locationRequest.interval = Constants.LOCATION_REQUEST_INTERVAL
        locationRequest.fastestInterval = Constants.LOCATION_REQUEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(), R.raw.map_style
            )
        )

        //check if permission granted
        checkIfPermissionGranted()
    }

    private fun checkIfPermissionGranted(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
            ){
                //permission granted, lets go
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                mMap.isMyLocationEnabled = true
            }
            else {
                //check for permission
                getDeviceLocationWithPermissionCheck()
            }
        } else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            mMap.isMyLocationEnabled = true
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //request permission here
            Toast.makeText(requireContext(), getString(R.string.permission_location_denied), Toast.LENGTH_LONG).show()
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        mMap.isMyLocationEnabled = true

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // NOTE: delegate the permission handling to generated function
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @OnPermissionDenied(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationPermissionDenied() {
        // NOTE: Deal with a denied permission, e.g. by showing specific UI
        // or disabling certain functionality
    }

    @OnShowRationale(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun showRationaleForLocation(request: PermissionRequest) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_location_rationale, request)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationPermissionNeverAskAgain() {

    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.grant_permission)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }

    private fun enableLocationSettings() {

        val locationRequest = LocationRequest.create()
            .setInterval(Constants.LOCATION_UPDATE_INTERVAL)
            .setFastestInterval(Constants.LOCATION_UPDATE_FASTEST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(requireContext())
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                requireActivity()
            ) { response: LocationSettingsResponse? -> }
            .addOnFailureListener(
                requireActivity()
            ) { ex: Exception? ->

                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        val intentSenderRequest = IntentSenderRequest.Builder(ex.resolution).build()
                        resolutionForResult.launch(intentSenderRequest)

                    } catch (sendEx: IntentSender.SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }


    private fun checkIfLocationIsTurnedOn(){

        if (GeneralUtility.isGPSEnabled(requireContext())){
            //All location services are disabled, //change lyt view
            binding.txtWantBetterPickups.text = getString(R.string.do_you_know_that)
            binding.txtShareLocation.text = getString(R.string.you_are_awesome)
            binding.icLocation.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_thumbs_up))
        }

        else{
            //all location services are enabled, //change lyt view
            binding.txtWantBetterPickups.text = resources.getString(R.string.want_better_pickups)
            binding.txtShareLocation.text = resources.getString(R.string.share_your_location)
            binding.icLocation.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_location_point))
        }
    }

    private fun handleClicks(){

        //on click txt share location
        binding.txtShareLocation.setOnClickListener {
            enableLocationSettings()
        }

        //on click about app
        binding.icAbout.setOnClickListener {
            openAlertDialogForAbout()
        }

        //on click where are you going to
        binding.etWhereGoingTo.setOnClickListener {
            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToInputDestinationFragment())
        }

    }

    private fun openAlertDialogForAbout(){

        //show an alert builder
        val sortView = layoutInflater.inflate(R.layout.lyt_about_app, null)

        val builder : android.app.AlertDialog = android.app.AlertDialog.Builder(
            requireContext(),
            R.style.Style_Dialog_Rounded_Corner
        )
            .setView(sortView)
            .create()

        //on click email icon
        sortView.findViewById<ImageView>(R.id.ic_email).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            val data = Uri.parse("mailto:ibrajix@gmail.com?subject=Hey!")
            intent.data = data
            startActivity(intent)
            builder.dismiss()
        }

        //on click twitter icon
        sortView.findViewById<ImageView>(R.id.ic_twitter).setOnClickListener {
            val intent: Intent = try {
                // Check if the Twitter app is installed on the phone.
                context?.packageManager?.getPackageInfo("com.twitter.android", 0)
                Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?user_id=997129876275593216"))
            } catch (e: Exception) {
                Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/ibrajix"))
            }
            context?.startActivity(intent)
            builder.dismiss()
        }

        //on click web icon
        sortView.findViewById<ImageView>(R.id.ic_web).setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://linktr.ee/Ibrajix"))
            startActivity(intent)
            builder.dismiss()
        }

        builder.create()
        builder.show()

    }


}