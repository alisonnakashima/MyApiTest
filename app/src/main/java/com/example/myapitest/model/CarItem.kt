package com.example.myapitest.model

data class CarDetails (
    val id: String,
    val imageUrl: String,
    val year: String,
    val name: String,
    val license: String,
    val place: ItemLocation?
)

data class CarItem (
    val id: String,
    val values: CarDetails
)


data class ItemLocation(
    val lat: Float,
    val long: Float
)

//"id": "001",
//    "imageUrl": "https://example.com/car1.jpg",
//    "year": "2020/2021",
//    "name": "Carro A",
//    "license": "ABC-1234",
//    "place": { "lat": -23.5505, "long": -46.6333 }