package com.devinfusion.hikisansih.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.devinfusion.hikisansih.MainActivity
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.dao.UserDao
import com.devinfusion.hikisansih.databinding.ActivityProfileBinding
import com.devinfusion.hikisansih.model.Farm
import com.devinfusion.hikisansih.model.Kisan
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding : ActivityProfileBinding
    private var uid : String? = ""
    private lateinit var auth : FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        uid = auth.currentUser!!.uid

        binding.mobileEt.setText(auth.currentUser!!.phoneNumber)

        binding.startBt.setOnClickListener {
            if (binding.nameEt.text.toString().trim().isNotEmpty() &&binding.ageEt.text.toString().trim().isNotEmpty()
                &&binding.mobileEt.text.toString().trim().isNotEmpty() &&binding.locationEt.text.toString().trim().isNotEmpty()){

                val farm = Farm("","")
                val kisan = Kisan(uid,binding.mobileEt.text.toString().trim(),binding.nameEt.text.toString().trim(),
                    binding.ageEt.text.toString().trim(),binding.locationEt.text.toString().trim(),0,"",farm
                )
                uploadDataToFirebase(kisan)
            }
        }

    }
    private fun uploadDataToFirebase(kisan: Kisan) {
        val userDao = UserDao()
        userDao.userCollection.child(kisan.uid.toString()).setValue(kisan).addOnSuccessListener {
            Toast.makeText(this@ProfileActivity, "Profile created successfully", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
            finish()
        }.addOnFailureListener {
            Toast.makeText(this@ProfileActivity, "something went wrong ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}