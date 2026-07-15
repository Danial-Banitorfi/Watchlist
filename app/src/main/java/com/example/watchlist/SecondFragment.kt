package com.example.watchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.watchlist.databinding.FragmentSecondBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * SECOND FRAGMENT (Registration):
 * Dieser Bildschirm ermöglicht es neuen Benutzern, ein Konto bei Firebase zu erstellen.
 */
class SecondFragment : Fragment(R.layout.fragment_second) {

    // View Binding: Ermöglicht den Zugriff auf XML-Elemente ohne findViewById
    private var _binding: FragmentSecondBinding? = null
    private val binding get() = _binding!!
    
    // FirebaseAuth: Das Werkzeug zur Verwaltung von Benutzern (Registrierung/Login)
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSecondBinding.bind(view)
        
        // Initialisierung des Firebase-Dienstes
        auth = FirebaseAuth.getInstance()

        /**
         * REGISTRIERUNGS-LOGIK:
         * Wird beim Klick auf den Registrierungs-Button ausgeführt.
         */
        binding.btnRegister.setOnClickListener {
            // Auslesen der Benutzereingaben
            val email = binding.etEmailRegister.text.toString()
            val password = binding.etPasswordRegister.text.toString()
            val passwordConfirm = binding.etPasswordConfirmRegister.text.toString()

            // 1. Validierung: Sind alle Felder ausgefüllt?
            if (email.isNotEmpty() && password.isNotEmpty() && passwordConfirm.isNotEmpty()) {
                
                // 2. Validierung: Stimmen Passwort und Bestätigung überein?
                if (password == passwordConfirm) {
                    /**
                     * createUserWithEmailAndPassword:
                     * Sendet die Registrierungsdaten an Firebase.
                     * Dies ist eine asynchrone Operation (läuft im Hintergrund).
                     */
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            // Prüfen, ob das Fragment noch aktiv ist, bevor wir das UI ändern
                            if (_binding != null) {
                                if (task.isSuccessful) {
                                    // Erfolg: Konto wurde angelegt
                                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    // Zurück zum Login-Bildschirm (FirstFragment)
                                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                                } else {
                                    // Fehler: Z.B. E-Mail existiert bereits oder ist ungültig
                                    Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                } else {
                    // Fehler: Passwörter sind unterschiedlich
                    Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Fehler: Felder sind leer
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation zurück zum Login-Bildschirm
        binding.tvGoToLogin.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
    }

    /**
     * Reinigung: Löscht das Binding, wenn die View zerstört wird, um Speicherlecks zu vermeiden.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
