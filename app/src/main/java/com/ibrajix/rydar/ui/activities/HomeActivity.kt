package com.ibrajix.rydar.ui.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.ibrajix.rydar.R
import com.ibrajix.rydar.databinding.ActivityHomeBinding
import com.ibrajix.rydar.utils.GeneralUtility.transparentStatusBar
import permissions.dispatcher.*
import android.content.IntentSender
import android.content.IntentSender.SendIntentException
import android.util.Log
import androidx.annotation.Nullable

import com.google.android.gms.common.api.ResolvableApiException

import com.google.android.gms.location.LocationSettingsResponse

import com.google.android.gms.location.LocationServices

import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.ibrajix.rydar.utils.Constants.LOCATION_UPDATE_FASTEST_INTERVAL
import com.ibrajix.rydar.utils.Constants.LOCATION_UPDATE_INTERVAL
import com.ibrajix.rydar.utils.Constants.REQUEST_CODE_CHECK_SETTINGS
import java.lang.Exception
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.ibrajix.rydar.utils.GeneralUtility.isGPSEnabled


@RuntimePermissions
class HomeActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar()

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        handleClicks()

        checkIfLocationIsTurnedOn()
    }

    private fun checkIfLocationIsTurnedOn(){

        if (isGPSEnabled(this)){

            //get device location
            getDeviceLocation()

            //All location services are disabled, //change lyt view
            binding.txtWantBetterPickups.text = getString(R.string.do_you_know_that)
            binding.txtShareLocation.text = getString(R.string.you_are_awesome)
            binding.icLocation.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_thumbs_up))

        }

        else{

            //All location services are enabled, //change lyt view
            binding.txtWantBetterPickups.text = resources.getString(R.string.want_better_pickups)
            binding.txtShareLocation.text = resources.getString(R.string.share_your_location)
            binding.icLocation.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_location_point))

        }

    }


    private fun handleClicks(){

        //on click txt share location
        binding.txtShareLocation.setOnClickListener {
            enableLocationSettings()
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        getDeviceLocationWithPermissionCheck()
        /*val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/
    }

    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {

            val locationResult = fusedLocationProviderClient.lastLocation

            locationResult.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    //Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.result
                    if (lastKnownLocation != null) {
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude), 15.toFloat()))
                    }
                    else{
                        requestLocation(this)
                    }
                } else {
                    Log.e("tsk", task.exception.toString())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(35.6892, 51.3890), 15.toFloat()))
                    mMap.uiSettings?.isMyLocationButtonEnabled = false
                }
            }
        } catch (e: SecurityException) {
            /*logD("Exception: ${e.message}")*/
        }
    }


    private fun requestLocation(context: Context) {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val mLocationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    if (location != null && lastKnownLocation == null) {
                        lastKnownLocation = location
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(context)
                .requestLocationUpdates(mLocationRequest, mLocationCallback, null)
        }


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
        showRationaleDialog(R.string.permission_camera_rationale, request)
    }

    @OnNeverAskAgain(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
    fun onLocationPermissionNeverAskAgain() {

    }

    private fun showRationaleDialog(@StringRes messageResId: Int, request: PermissionRequest) {
        AlertDialog.Builder(this)
            .setPositiveButton(R.string.button_allow) { _, _ -> request.proceed() }
            .setNegativeButton(R.string.button_deny) { _, _ -> request.cancel() }
            .setCancelable(false)
            .setMessage(messageResId)
            .show()
    }


    private fun enableLocationSettings() {

        val locationRequest = LocationRequest.create()
            .setInterval(LOCATION_UPDATE_INTERVAL)
            .setFastestInterval(LOCATION_UPDATE_FASTEST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        LocationServices
            .getSettingsClient(this)
            .checkLocationSettings(builder.build())
            .addOnSuccessListener(
                this
            ) { response: LocationSettingsResponse? -> }
            .addOnFailureListener(
                this
            ) { ex: Exception? ->
                if (ex is ResolvableApiException) {
                    // Location settings are NOT satisfied,  but this can be fixed  by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),  and check the result in onActivityResult().
                        ex.startResolutionForResult(
                            this,
                            REQUEST_CODE_CHECK_SETTINGS
                        )
                    } catch (sendEx: SendIntentException) {
                        // Ignore the error.
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_CODE_CHECK_SETTINGS == requestCode){
            if (RESULT_OK == resultCode){
                //user clicked OK, you can startUpdatingLocation(...);
                getDeviceLocation()
            }
            else{
                //user clicked cancel: informUserImportanceOfLocationAndPresentRequestAgain();
            }
        }
    }


    override fun onResume() {
        super.onResume()
        checkIfLocationIsTurnedOn()
    }


}