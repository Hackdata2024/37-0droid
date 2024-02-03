package com.devinfusion.hikisansih.dao

import android.widget.Toast
import com.devinfusion.hikisansih.model.Kisan
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserDao {
    private val db = FirebaseDatabase.getInstance()
    public val farmCollection = db.getReference("Farm")
    public val userCollection = db.getReference("User")

    private val auth = FirebaseAuth.getInstance()


    fun addUser(kisan : Kisan){
        kisan?.let {
            userCollection.child(kisan.uid.toString()).setValue(kisan)
        }
    }

    fun getUserById(uid : String) : Task<DataSnapshot> {
        return userCollection.child(uid).get()
    }


}