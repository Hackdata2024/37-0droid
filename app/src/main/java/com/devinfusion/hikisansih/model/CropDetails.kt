package com.devinfusion.hikisansih.model


data class CropDetails(
    val name : String,
    val temperatureRange: String,
    val duration: Int, // in days
    val seedRate: Int, // in kg per hectare
    val seedTreatment: String,
    val seedPreparation: String,
    val image : Int
)