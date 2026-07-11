package com.example.watchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.watchlist.databinding.FragmentSecondBinding
import com.google.firebase.auth.FirebaseAuth

class SecondFragment : Fragment(R.layout.fragment_second) {

    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSecondBinding.bind(view)
        auth = FirebaseAuth.getInstance()

        // Button zum Erstellen eines Kontos
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val passwordConfirm = binding.etPasswordConfirmRegister.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                if (password == passwordConfirm) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (_binding != null) {
                                if (task.isSuccessful) {
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    // Nach Registrierung zurück zum Login oder direkt weiter
                                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                                } else {
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Zurück zum Login
        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}