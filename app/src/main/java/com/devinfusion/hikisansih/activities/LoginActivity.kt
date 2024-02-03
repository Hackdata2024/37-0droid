package com.devinfusion.hikisansih.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.devinfusion.hikisansih.MainActivity
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding : ActivityLoginBinding
    private var mainView : Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getStartedButton.setOnClickListener {
            // Check if the mainView is visible
            if (binding.mainView.visibility == View.VISIBLE) {
                mainView = 0
                // Animate the fade-out of mainView
                binding.mainView.animate()
                    .alpha(0f)
                    .setDuration(300) // Animation duration in milliseconds
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.mainView.visibility = View.GONE
                            binding.fragmentContainer.visibility = View.VISIBLE
                        }
                    })
                    .start()
            }
        }
    }

    override fun onBackPressed() {
        if (mainView == 0){
            mainView = 1
            if (binding.mainView.visibility == View.GONE){
                binding.mainView.alpha = 0f
                binding.mainView.visibility = View.VISIBLE
                binding.mainView.animate()
                    .alpha(1f)
                    .setDuration(300)
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            binding.fragmentContainer.visibility = View.GONE
                        }
                    })
                    .start()
            }
        }else{
            super.onBackPressed()
        }

    }

    override fun onStart() {
        super.onStart()
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser!=null){
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }
    }
}