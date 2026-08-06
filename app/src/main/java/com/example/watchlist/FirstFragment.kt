package com.example.watchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.watchlist.databinding.FragmentFirstBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * FIRST FRAGMENT (Login):
 * Der erste Bildschirm, den der User sieht. Hier wird die Authentifizierung mit Firebase abgewickelt.
 */
class FirstFragment : Fragment(R.layout.fragment_first) {

    // _binding ist die eigentliche "Speicherbox" für unsere UI-Elemente.
    private var _binding: FragmentFirstBinding? = null

    // binding ist unser "bequemer Zugriff".
    // Durch 'get() = _binding!!' sparen wir uns das ständige '?' im restlichen Code.
    private val binding get() = _binding!!
    
    // FirebaseAuth ist das Objekt, das die Verbindung zum Google-Server hält
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFirstBinding.bind(view)
        
        // Initialisiert die Firebase-Instanz
        auth = FirebaseAuth.getInstance()

        /**
         * AUTO-LOGIN CHECK:
         * Wenn der User sich schon mal eingeloggt hat, erinnert sich Firebase daran.
         * currentUser ist dann nicht null, und wir können direkt zum Home springen.
         */
        if (auth.currentUser != null) {
            findNavController().navigate(R.id.HomeFragment)
            return
        }

        /**
         * LOGIN-LOGIK:
         * Wird beim Klick auf den Login-Button ausgeführt.
         */
        binding.btnLogin.setOnClickListener {
            // .text.toString() wandelt den Inhalt der Eingabefelder in verarbeitbaren Text um
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()

            // Einfache Prüfung: Felder dürfen nicht leer sein
            if (email.isNotEmpty() && password.isNotEmpty()) {
                /**
                 * signInWithEmailAndPassword:
                 * Schickt die Daten an Firebase. Das passiert asynchron (im Hintergrund).
                 * .addOnCompleteListener gibt uns Bescheid, wenn der Server fertig ist.
                 */
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Erfolg: User darf rein
                            Toast.makeText(context, "Login successful!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.HomeFragment)
                        } else {
                            // Fehler: Z.B. falsches Passwort oder keine Internetverbindung
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigation zum Registrierungs-Bildschirm
        binding.tvGoToRegister.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        /**
         * FORGOT PASSWORD LOGIC:
         * Ermöglicht es dem User, eine E-Mail zum Zurücksetzen des Passworts zu erhalten.
         * Firebase übernimmt dabei den Versand der E-Mail und die Bereitstellung der Webseite
         * zum Ändern des Passworts.
         */
        binding.tvForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isNotEmpty()) {
                // Firebase-Funktion zum Senden der Reset-E-Mail
                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Erfolg: E-Mail wurde verschickt
                            Toast.makeText(context, "Reset email sent to $email", Toast.LENGTH_LONG).show()
                        } else {
                            // Fehler: Z.B. E-Mail-Adresse nicht in der Datenbank
                            Toast.makeText(context, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                // Hinweis: Der User muss erst seine E-Mail eingeben, damit wir wissen, wohin die Mail soll
                Toast.makeText(context, "Please enter your email address first", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
