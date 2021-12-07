package com.ibrajix.rydar.ui.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.FragmentInputDestinationBinding
import com.ibrajix.rydar.utils.Constants
import permissions.dispatcher.*
import java.io.IOException
import java.util.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import android.widget.TextView.OnEditorActionListener
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibrajix.rydar.data.LocationDirectionModel
import com.ibrajix.rydar.data.Resource
import com.ibrajix.rydar.ui.adapters.SearchLocationAdapter
import com.ibrajix.rydar.ui.viewmodel.MainViewModel
import com.ibrajix.rydar.utils.Constants.DELAY_SEARCH_QUERY
import com.ibrajix.rydar.utils.GeneralUtility.displaySnackBar
import com.ibrajix.rydar.utils.GeneralUtility.hideKeyboard
import com.ibrajix.rydar.utils.GeneralUtility.isNetworkAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


@SuppressLint("MissingPermission")
@RuntimePermissions
@AndroidEntryPoint
class InputDestinationFragment : Fragment(), OnMapReadyCallback {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var searchLocationAdapter: SearchLocationAdapter
    private var _binding: FragmentInputDestinationBinding? = null
    private val binding get() = _binding!!
    var geocoder: Geocoder? = null
    var addresses: List<Address>? = null
    var job: Job? = null

    var fromPoint1: Double? = null
    var fromPoint2: Double? = null
    var toPoint1: Double? = null
    var toPoint2: Double? = null
    var midPoint1: Double? = null
    var midPoint2: Double? = null

    //map variables
    private lateinit var mMap: GoogleMap
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    //location call back
    private var locationCallback: LocationCallback = object : LocationCallback() {

        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            val locationList = locationResult?.locations
            if (locationList != null) {
                if (locationList.isNotEmpty()) {
                    //the last location is the most recent or newest
                    val location = locationList.last()
                    //get location text value with geocoder
                    try {
                        addresses = geocoder?.getFromLocation(location.latitude, location.longitude, 1)
                        val address = addresses?.get(0)?.getAddressLine(0)
                        fromPoint1 = location.latitude
                        fromPoint2 = location.longitude
                        val addressFormatted = address.toString().lowercase(Locale.getDefault()).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                        binding.etFrom.setText(addressFormatted)
                    } catch (e: IOException) {
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
        _binding = FragmentInputDestinationBinding.inflate(inflater, container, false)
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
        setUpRecyclerView()
        setUpSearch()
        setUpObserver()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPause() {
        super.onPause()
        //stop location updates when activity is no longer active
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setUpRecyclerView(){
        searchLocationAdapter = SearchLocationAdapter(SearchLocationAdapter.OnLocationItemClickListener{ destination->

            toPoint1 = destination.geometry.location.lat
            toPoint2 = destination.geometry.location.lng
            midPoint1 = destination.geometry.location.lat
            midPoint2 = destination.geometry.location.lng
            hideKeyboard(requireActivity())
            val locationModel = LocationDirectionModel(fromPoint1, fromPoint2, toPoint1, toPoint2, midPoint1, midPoint2)
            val action = InputDestinationFragmentDirections.actionInputDestinationFragmentToConfirmDestinationFragment(locationModel)
            findNavController().navigate(action)
        })
        binding.rcvSearchResult.adapter = searchLocationAdapter
        binding.rcvSearchResult.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }

    private fun setUpObserver(){

       lifecycleScope.launch {
           repeatOnLifecycle(Lifecycle.State.STARTED) {
               mainViewModel.searchLocation.collect {

                   when (it.status) {
                       Resource.Status.SUCCESS -> {
                           if (it.data?.status == "OK") {
                               searchLocationAdapter.submitList(it.data.candidates)
                           } else {
                               displaySnackBar(
                                   binding.root,
                                   "",
                                   requireContext()
                               )
                           }
                       }

                       Resource.Status.ERROR -> {
                           //handle error here by showing a snackBar
                           displaySnackBar(
                               binding.root,
                               it.message?:"",
                               requireContext()
                           )
                       }

                       Resource.Status.FAILURE -> {
                           //handle failure here
                           displaySnackBar(
                               binding.root,
                               it.message?:"",
                               requireContext()
                           )
                       }

                   }

               }
           }
       }

    }

    private fun setUpSearch(){
        binding.etTo.addTextChangedListener { editable->
            job?.cancel()
            job = MainScope().launch {
                delay(DELAY_SEARCH_QUERY)
            }
            editable?.let {
                if (editable.toString().isNotEmpty()){
                    if (isNetworkAvailable(requireContext())){
                        mainViewModel.doSearchLocation(editable.toString())
                    }
                    else{
                        displaySnackBar(
                            binding.root,
                            resources.getString(R.string.not_connected_to_internet),
                            requireContext()
                        )
                    }
                }
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {

        //init map items
        mMap = googleMap
        locationRequest = LocationRequest()
        locationRequest.interval = Constants.LOCATION_REQUEST_INTERVAL
        locationRequest.fastestInterval = Constants.LOCATION_REQUEST_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

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
                getDeviceLocationForInputWithPermissionCheck()
            }
        } else{
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
            mMap.isMyLocationEnabled = true
        }
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun getDeviceLocationForInput() {
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

    private fun handleClicks(){

        //on click back
        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }

        //on click search edit text from
        binding.etTo.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (isNetworkAvailable(requireContext())){
                    mainViewModel.doSearchLocation(binding.etTo.text.toString())
                }
                else{
                    displaySnackBar(
                        binding.root,
                        resources.getString(R.string.not_connected_to_internet),
                        requireContext()
                    )
                }
                return@OnEditorActionListener true
            }
            false
        })

    }

}