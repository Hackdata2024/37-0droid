package com.devinfusion.hikisansih.model

data class Kisan(
    val uid: String? = null,
    val mobileNumber: String? = null,
    val name: String? = null,
    val age: String? = null,
    val location: String? = null,
    val loan : Int? = null,
    val farmBought : String? = null,
    val farm: Farm? = null
) {
    constructor() : this(null, null, null, null, null, null,null)
}


