package com.devinfusion.hikisansih.model


data class WeatherRvModel(
    val id : Int,
    val name: String,
    val speed: String,
    val humidity: String,
    val temp: Int,
    val temp_max: String,
    val temp_min: String,
    val icon: String,
    val forecast: String,
    val lastupdatedAt : Long,
    val state : String
)