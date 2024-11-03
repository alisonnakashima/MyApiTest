package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapitest.databinding.ActivityItemDetailBinding
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.CarItem
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItemDetailBinding
    private lateinit var carItem: CarItem
    private lateinit var carDetails: CarDetails


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItemDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        loadItem()
    }

    private fun setupView() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
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
        binding.carNameTv.text = "${carItem.values.name}"
        binding.yearTv.text = "${carItem.values.year}"
        binding.yearTv.text = "${carItem.values.license}"

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

}