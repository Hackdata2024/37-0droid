package com.devinfusion.hikisansih.fragment.phoneAuth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.devinfusion.hikisansih.MainActivity
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.activities.ProfileActivity
import com.devinfusion.hikisansih.dao.UserDao
import com.devinfusion.hikisansih.databinding.FragmentOTPBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.concurrent.TimeUnit


class OTPFragment : Fragment() {

    private var _binding : FragmentOTPBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth


    private lateinit var number : String
    private lateinit var OTP : String
    private lateinit var resendToken : PhoneAuthProvider.ForceResendingToken

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOTPBinding.inflate(layoutInflater,container,false)

        inti()

        number = arguments?.getString("phoneNumber")!!
        OTP = arguments?.getString("OTP")!!
        resendToken = arguments?.getParcelable<PhoneAuthProvider.ForceResendingToken>("resendToken")!!

        binding.continueButton.setOnClickListener {
            val otp : String = binding.otpEt.text.toString().trim()
            if (otp.isNotEmpty()){
                if (otp.length<6){
                    Toast.makeText(requireContext(), "Enter valid otp", Toast.LENGTH_SHORT).show()
                }
                else{
                    binding.progressBar.visibility = View.VISIBLE
                    binding.continueButton.visibility = View.INVISIBLE
                    val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(OTP!!,otp)
                    signInWithPhoneAuthCredential(credential)
                }
            }
            else{
                Toast.makeText(requireContext(), "Field should not be empty", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }
    fun inti(){
        auth = FirebaseAuth.getInstance()
    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(requireContext(), "Error : ${p0.message}", Toast.LENGTH_SHORT).show()
            binding.progressBar.visibility = View.INVISIBLE
            binding.continueButton.visibility = View.VISIBLE
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            OTP = verificationId
            resendToken = token
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    updateUI(task.result.user)
                } else {
                    Toast.makeText(requireContext(), "SignIn Failed try again", Toast.LENGTH_SHORT).show()
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.continueButton.visibility = View.VISIBLE
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user!=null){
            binding.progressBar.visibility = View.VISIBLE
            binding.continueButton.visibility = View.INVISIBLE
            checkUserExists(user)
        }
    }
    private fun checkUserExists(user: FirebaseUser?) {
        val context = context

        if (context != null) {
            val userDao = UserDao()
            userDao.userCollection.child(user!!.uid).addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        startActivity(Intent(context, MainActivity::class.java))
                        requireActivity().finish()
                    } else {
                        val i = Intent(context, ProfileActivity::class.java)
                        i.putExtra("uid", user.uid)
                        startActivity(i)
                        requireActivity().finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("errorAgaya", error.message)
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Handle the case where the context is null
            Log.e("Error", "Fragment is not attached to a context")
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}