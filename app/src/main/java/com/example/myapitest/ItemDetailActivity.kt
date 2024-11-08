package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityItemDetailBinding
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.CarItem
import com.example.myapitest.model.ItemLocation
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.ui.CircleTransform
import com.example.myapitest.ui.loadUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.internal.wait

class ItemDetailActivity : AppCompatActivity(), OnMapReadyCallback{


    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var carItem: CarItem
    private lateinit var carDetails: CarDetails

    private lateinit var mMap:GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadItem()
        setupGoogleMap()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (::carItem.isInitialized){
            loadItemLocationInGoogleMap()
        }
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.deleteCTA.setOnClickListener{
            deleteItem()
        }

        binding.saveEditedCTA.setOnClickListener{
            saveEditedItem()
        }

        binding.carNameTv.setOnClickListener{
            editName()
        }
    }

    private fun setupGoogleMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }
    private fun loadItem(){
        val itemId = intent.getStringExtra(ARG_ID) ?: ""

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCar(itemId) }

            withContext(Dispatchers.Main) {
                when(result){
                    is Result.Error -> {}
                    is Result.Success -> {
                        carItem = result.data
                        handleSucess()
                    }
                }
            }
        }
    }

    private fun handleSucess (){
        binding.carNameTv.text = "${carItem.value.name}"
        binding.yearTv.text = "${carItem.value.year}"
        binding.licenseTv.text = "${carItem.value.license}"
        carItem.value.imageUrl?.let { binding.imageIv.loadUrl(it) }
        loadItemLocationInGoogleMap()
    }

    private fun deleteItem() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.deleteCar(carItem.id) }

            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.delete_error,
                            Toast.LENGTH_SHORT).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.delete_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun saveEditedItem() {

        binding.carNameEt.visibility = View.GONE
        binding.carNameTv.visibility = View.VISIBLE
        binding.carNameTv.text = binding.carNameEt.text.toString()
//        val teste = binding.carNameEt.text.toString()

        // Fecha o teclado
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.carNameEt.windowToken, 0)

        carItem.value.copy(imageUrl = binding.imageIv.toString())
        carItem.value.copy(year = binding.yearTv.text.toString())
        carItem.value.name = binding.carNameTv.text.toString()
        carItem.value.copy(license = binding.licenseTv.text.toString())

//        carItem.value.copy(place = binding.licenseTv.text.toString())
//        var locationEmpty = ItemLocation(lat= ((-43.172).toFloat()), long= ((-43.172).toFloat()))
//        carItem.value.place = ItemLocation(lat= ((-43.172).toFloat()), long= ((-43.172).toFloat()))

        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall {
                RetrofitClient.apiService.updateCar(
                    carItem.value.id,
                    carItem.value
                )
            }
            withContext(Dispatchers.Main) {
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.update_error,
                            Toast.LENGTH_SHORT).show()
                    }

                    is Result.Success -> {
                        Toast.makeText(
                            this@ItemDetailActivity,
                            R.string.update_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }
            }
        }
    }

    private fun editName(){
        binding.carNameTv.visibility = View.GONE
        binding.carNameEt.setText(binding.carNameTv.text.toString())
        binding.carNameEt.visibility = View.VISIBLE

        // Abre o teclado automaticamente
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.carNameEt, InputMethodManager.SHOW_IMPLICIT)
    }

    companion object {

        private const val ARG_ID = "ARG_ID"

        fun newIntent (
            context: Context,
            itemId: String
        ) =
            Intent (context, ItemDetailActivity::class.java).apply {
                putExtra(ARG_ID, itemId)
            }
    }


    private fun loadItemLocationInGoogleMap() {
        carItem.value.place?.let {
            binding.googleMapContent.visibility = View.VISIBLE
            val latLong = LatLng(it.lat.toDouble(), it.long.toDouble())
            Log.i("latlong", "${it.lat}, ${it.long}")
            // Adiciona pin no Map
            mMap.addMarker(
                MarkerOptions()
                    .position(latLong)
                    .title("Local Selecionado")
            )
            // Move a camera do Map para a mesma localização do Pin
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLong,
                    17f
                )
            )
        }
    }


}