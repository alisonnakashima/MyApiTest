package com.example.myapitest

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.ItemLocation
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.common.collect.MapMaker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.SecureRandom

class NewItemActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityNewItemBinding

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var selectedMapMarker: Marker? = null

    private lateinit var imageUri: Uri
    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupGoogleMap()
        setupView()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        binding.googleMapContentOnAddCar.visibility = View.VISIBLE
        getDeviceLocation()
        mMap.setOnMapClickListener { latLng: LatLng ->
            // Limpar marcador anterior, se existir
            selectedMapMarker?.remove()

            selectedMapMarker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .draggable(true)
                    .title("Lat: ${latLng.latitude}, Long: ${latLng.longitude}")
            )
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapOnAddCar) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun getDeviceLocation() {
        //CHECKING PERMISSION
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadCurrentLocation()
        } else {
            //REQUEST PERMISSION WHEN IT´S DENIED
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun loadCurrentLocation() {
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
            val currentLocation = LatLng(location.latitude, location.longitude)
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    currentLocation,
                    17f
                )
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                loadCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    R.string.denied_permission,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.saveCta.setOnClickListener{
            save()
        }
    }

    private fun save() {
        if (!validateForm()) return
        CoroutineScope(Dispatchers.IO).launch {
            val id = SecureRandom().nextInt().toString()
            // variável provisória para verificação posterior e inserção da location via GPS
            val locationEmpty= ItemLocation(lat= ((-43.172).toFloat()), long= ((-43.172).toFloat()))
            val itemValue = CarDetails (
                id,
                binding.imageUrl.text.toString(),
                binding.carYear.text.toString(),
                binding.carName.text.toString(),
                binding.carLicense.text.toString(),
                locationEmpty
            )
            val result = safeApiCall { RetrofitClient.apiService.addCar(itemValue) }
            withContext(Dispatchers.Main) {
                when (result){
                    is Result.Error -> {
                        Toast.makeText(
                            this@NewItemActivity,
                            getString(R.string.create_error),
                            Toast.LENGTH_SHORT
                            ).show()
                    }
                    is Result.Success -> {
                        Toast.makeText(
                            this@NewItemActivity,
                            getString(R.string.create_sucess, result.data.id),
                            Toast.LENGTH_LONG
                            ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun validateForm(): Boolean {
        if (binding.carName.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Name"), Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.carYear.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Surname"), Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.carLicense.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Age"), Toast.LENGTH_SHORT).show()
            return false
        }
        if (binding.imageUrl.text.toString().isBlank()) {
            Toast.makeText(this, getString(R.string.error_validate_form, "Image Url"), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }


    companion object{

        private const val LOCATION_PERMISSION_REQUEST_CODE = 100

        fun newIntent (context: Context) =
            Intent(context, NewItemActivity::class.java)
    }


}