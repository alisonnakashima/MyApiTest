package com.example.myapitest

// 1- Criar tela de Login com algum provedor do Firebase (Telefone, Google)
//      Cadastrar o Seguinte celular para login de test: +5511912345678
//      Código de verificação: 101010

// 2- Criar Opção de Logout no aplicativo

// 3- Integrar API REST /car no aplicativo
//      API será disponibilida no Github
//      JSON Necessário para salvar e exibir no aplicativo
//      O Image Url deve ser uma foto armazenada no Firebase Storage
//      { "id": "001", "imageUrl":"https://image", "year":"2020/2020", "name":"Gaspar", "licence":"ABC-1234", "place": {"lat": 0, "long": 0} }

// Opcionalmente trabalhar com o Google Maps ara enviar o place

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapitest.adapter.ItemAdapter
import com.example.myapitest.databinding.ActivityMainBinding
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.CarItem
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestLocationPermission()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        fetchItems()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = LoginActivity.newIntent(this)
//                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupView() {
        binding.recyclerView.layoutManager = LinearLayoutManager (this)
        binding.swipeRefreshLayout.setOnRefreshListener {
            binding.swipeRefreshLayout.isRefreshing = true
            fetchItems()
        }
        binding.addCta.setOnClickListener{
            startActivity(NewItemActivity.newIntent(this))
        }
    }

    private fun requestLocationPermission() {
        //inicialização da localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        //configuração do launcher para request permission
        locationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {isGranted ->
            if (isGranted){
                getLastLocation()
            } else {
                Toast.makeText(
                    this,
                    R.string.denied_permission,
                    Toast.LENGTH_LONG)
                    .show()
            }
        }
        checkLocationPermissionAndRequest()
    }

    private fun checkLocationPermissionAndRequest(){
        when {
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                getLastLocation()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION) -> {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_COARSE_LOCATION) -> {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
            }
            else ->{
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun getLastLocation(){
        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission()
            return
        }
        fusedLocationClient.lastLocation.addOnCompleteListener{task: Task<Location> ->
            if (task.isSuccessful && task.result != null) {
                val location = task.result

                Toast.makeText(
                    this,
                    "Lat: ${location.latitude} Long: ${location.longitude}",
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(
                    this,
                    R.string.unknown_error,
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchItems() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCarsList() }
            withContext(Dispatchers.Main) {
                binding.swipeRefreshLayout.isRefreshing = false
                when (result) {
                    is Result.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            result.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    is Result.Success-> handleOnSuccess(result.data)
                }
            }
        }
    }

    private fun handleOnSuccess (data: List <CarDetails>) {
        val adapter = ItemAdapter(data){
            //click do item da lista
            startActivity(ItemDetailActivity.newIntent(
                this,
                it.id
            ))
        }
        binding.recyclerView.adapter = adapter
    }

    companion object {
        fun newIntent (context: Context) = Intent(context,MainActivity::class.java)
    }
}
