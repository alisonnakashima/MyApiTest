package com.example.myapitest.service

import com.example.myapitest.model.CarDetails
import com.example.myapitest.model.CarItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @GET("car")
    suspend fun getCarsList(): List<CarDetails>

    @GET("car/{id}")
    suspend fun getCar(@Path("id") id: String): CarItem

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): CarItem

    @POST("car")
    suspend fun addCar(@Body item: CarDetails): CarItem

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body item: CarDetails): CarDetails

}