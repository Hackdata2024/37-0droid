package com.devinfusion.hikisansih.fragment.phoneAuth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.devinfusion.hikisansih.MainActivity
import com.devinfusion.hikisansih.R
import com.devinfusion.hikisansih.databinding.FragmentPhoneAuthBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthFragment : Fragment() {
    private var _binding : FragmentPhoneAuthBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth : FirebaseAuth
    private lateinit var number : String
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPhoneAuthBinding.inflate(layoutInflater,container,false)

        init()

        binding.continueButton.setOnClickListener {
            number = binding.mobileEt.text.toString().trim()
            if (number.length < 10){
                Toast.makeText(requireContext(), "Enter a valid number", Toast.LENGTH_SHORT).show()
            }
            else{
                binding.progressBar.visibility = View.VISIBLE
                binding.continueButton.visibility = View.INVISIBLE
                number = "+91$number"
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(number)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(requireActivity())
                    .setCallbacks(callbacks)
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }
        }



        return binding.root
    }

    fun init(){
        auth = FirebaseAuth.getInstance()
        val edit : EditText = binding.mobileEt
        edit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                if (s.length == 10){
                    binding.continueButton.setBackgroundColor(resources.getColor(R.color.black))
                    binding.continueButton.isEnabled = true
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            Toast.makeText(requireContext(), "Error : ${p0.message}", Toast.LENGTH_SHORT).show()
            Log.d("ErrorAgaya",p0.message.toString() )
            Log.d("ErrorAgaya",number)
            binding.progressBar.visibility = View.INVISIBLE
            binding.continueButton.visibility = View.VISIBLE
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            val bundle = Bundle()
            bundle.putString("phoneNumber", number) // Replace 'phoneNumber' with your actual data
            bundle.putString("OTP",verificationId)
            bundle.putParcelable("resendToken",token)

            findNavController().navigate(R.id.action_phoneAuthFragment_to_OTPFragment,bundle)


//            val intent = Intent(requireContext(),MainActivity::class.java)
//            intent.putExtra("OTP",verificationId)
//            intent.putExtra("resendToken",token)
//            intent.putExtra("phoneNumber",number)
//            startActivity(intent)
        }

    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    updateUI(task.result.user)
                } else {
                    Toast.makeText(requireContext(), "Signin Failed try again", Toast.LENGTH_SHORT).show()
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.continueButton.visibility = View.VISIBLE
                }
            }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user!=null){
            binding.progressBar.visibility = View.VISIBLE
            binding.continueButton.visibility = View.INVISIBLE
            startActivity(Intent(requireContext(), MainActivity::class.java))
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}