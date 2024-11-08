package com.example.myapitest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapitest.databinding.ActivityNewItemBinding
import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.ItemLocation
import com.example.myapitest.service.Result
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom

class NewItemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewItemBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewItemBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
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
        fun newIntent (context: Context) =
            Intent(context, NewItemActivity::class.java)
    }
}