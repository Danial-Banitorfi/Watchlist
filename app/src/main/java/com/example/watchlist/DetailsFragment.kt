package com.example.watchlist

// Importe sind wie Werkzeuge, die wir aus dem Schrank holen
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.watchlist.api.TmdbClient
import com.example.watchlist.databinding.FragmentDetailsBinding
import com.example.watchlist.models.CreditsResponse
import com.example.watchlist.models.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Die Klasse für die Detail-Anzeige
class DetailsFragment : Fragment(R.layout.fragment_details) {

    // _binding ist die Verbindung zu den UI-Elementen im XML
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    
    // Konstanten für den API-Zugriff und die Datenbank
    private val API_KEY = "c804cebbae0a6ec42aecbf76a22d7a77"
    private val db = FirebaseFirestore.getInstance() // Startet die Firestore-Datenbank
    private val auth = FirebaseAuth.getInstance() // Startet das Benutzer-System

    // Wird aufgerufen, wenn die Seite im Handy geladen wurde
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Verbindet das Layout mit dieser Datei
        _binding = FragmentDetailsBinding.bind(view)

        // Holt den Film aus dem "Paket", das wir beim Klick gesendet haben
        // "as? Movie" bedeutet: Versuche es als Film-Objekt zu lesen
        val movie = arguments?.getSerializable("movie") as? Movie

        // Wenn ein Film gefunden wurde, zeige die Details an
        movie?.let {
            displayMovieDetails(it)
            loadCredits(it.id)
        }
    }

    // Zeigt Titel, Beschreibung und Cover an
    private fun displayMovieDetails(movie: Movie) {
        binding.tvDetailsTitle.text = movie.title
        binding.tvDetailsReleaseDate.text = "Veröffentlichung: ${movie.releaseDate}"
        binding.tvDetailsOverview.text = movie.description

        // Glide lädt das Bild aus dem Internet in das ImageView (ivDetailsPoster)
        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        Glide.with(this)
            .load(posterUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivDetailsPoster)
            
        // Button "Gesehen" - Syntax: setOnClickListener reagiert auf Fingertipp
        binding.btnMarkAsSeen.setOnClickListener {
            // Wir rufen unsere Speicher-Funktion mit dem Status "seen" auf
            saveMovieWithStatus(movie, "seen")
        }

        // Button "Geplant"
        binding.btnAddToPlanned.setOnClickListener {
            // Wir rufen unsere Speicher-Funktion mit dem Status "planned" auf
            saveMovieWithStatus(movie, "planned")
        }
    }

    // Speichert den Film in der Cloud
    private fun saveMovieWithStatus(movie: Movie, status: String) {
        // Holt die ID des aktuell angemeldeten Benutzers
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Erstellt eine Liste (Map) mit den Filmdaten und dem neuen Status
            // "to" verknüpft einen Namen (Key) mit einem Wert (Value)
            val movieData = hashMapOf(
                "id" to movie.id,
                "title" to movie.title,
                "posterPath" to movie.posterPath,
                "releaseDate" to movie.releaseDate,
                "overview" to movie.description, // Jetzt speichern wir auch die Beschreibung
                "status" to status // "seen" oder "planned"
            )

            // Pfad in der Datenbank: users -> UID -> watchlist -> MovieID
            db.collection("users")
                .document(currentUser.uid)
                .collection("watchlist")
                .document(movie.id.toString())
                .set(movieData) // Schreibt die Daten in das Dokument
                .addOnSuccessListener {
                    // Wird ausgeführt, wenn das Internet geklappt hat
                    val message = if (status == "seen") "Als gesehen markiert" else "Auf geplant gesetzt"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    // Wird ausgeführt, wenn ein Fehler auftritt
                    Toast.makeText(context, "Fehler: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Lädt Regisseur und Schauspieler von TMDB
    private fun loadCredits(movieId: Int) {
        TmdbClient.instance.getMovieCredits(movieId, API_KEY).enqueue(object : Callback<CreditsResponse> {
            override fun onResponse(call: Call<CreditsResponse>, response: Response<CreditsResponse>) {
                if (_binding != null && response.isSuccessful) {
                    val credits = response.body()
                    // Filtert die Crew nach dem Job "Director" (Regisseur)
                    val director = credits?.crew?.find { it.job == "Director" }?.name ?: "Unbekannt"
                    binding.tvDetailsDirector.text = "Regie: $director"
                    
                    // Nimmt die ersten 5 Schauspieler und trennt sie mit Komma
                    val actors = credits?.cast?.take(5)?.joinToString(", ") { it.name } ?: "Keine Infos"
                    binding.tvDetailsActors.text = "Schauspieler: $actors"
                }
            }

            override fun onFailure(call: Call<CreditsResponse>, t: Throwable) {
                if (_binding != null) {
                    binding.tvDetailsDirector.text = "Fehler beim Laden"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
