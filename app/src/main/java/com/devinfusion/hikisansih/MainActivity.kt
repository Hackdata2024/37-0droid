package com.devinfusion.hikisansih

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.devinfusion.hikisansih.activities.LoginActivity
import com.devinfusion.hikisansih.activities.ProfileActivity
import com.devinfusion.hikisansih.dao.UserDao
import com.devinfusion.hikisansih.databinding.ActivityMainBinding
import com.devinfusion.hikisansih.model.Kisan
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.paperdb.Paper

class MainActivity : AppCompatActivity() {
    private lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseDatabase
    private lateinit var binding : ActivityMainBinding
    private lateinit var userDao : UserDao
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Paper.init(this)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        val uid = auth.currentUser!!.uid
        userDao = UserDao()
        val databaseReference = userDao.userCollection.child(uid)

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(Kisan::class.java)
                    if (user != null) {
                        val navController = findNavController(R.id.fragment)

                        NavigationUI.setupWithNavController(binding.bottomNav,navController)

                        Paper.book().write("user", user)
                    }else{
                        Toast.makeText(this@MainActivity, "User is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    startActivity(Intent(this@MainActivity,ProfileActivity::class.java))
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        })



    }
    override fun onStart() {
        super.onStart()
        if (FirebaseAuth.getInstance().currentUser == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}