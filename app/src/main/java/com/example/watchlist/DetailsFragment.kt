package com.example.watchlist

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

/**
 * DETAILS FRAGMENT:
 * Zeigt die detaillierten Informationen eines ausgewählten Films an und ermöglicht
 * das Speichern in der persönlichen Watchlist.
 */
class DetailsFragment : Fragment(R.layout.fragment_details) {

    // View Binding: Sicherer Zugriff auf die XML-Elemente
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    
    // API-Key für TMDB und Instanzen für Firebase Dienste
    private val API_KEY = "c804cebbae0a6ec42aecbf76a22d7a77"
    private val db = FirebaseFirestore.getInstance() // Cloud-Datenbank
    private val auth = FirebaseAuth.getInstance() // Authentifizierung

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDetailsBinding.bind(view)

        /**
         * DATEN-EMPFANG (Arguments):
         * Wir holen das Movie-Objekt aus dem Bundle (Paket), das uns das 
         * HomeFragment oder WatchlistFragment geschickt hat.
         * 'as? Movie' ist ein sicherer Cast (Cast-Operator).
         */
        val movie = arguments?.getSerializable("movie") as? Movie

        // Wenn der Film nicht null ist (let-Block), zeigen wir ihn an
        movie?.let {
            displayMovieDetails(it)
            loadCredits(it.id) // Lädt Regie und Schauspieler nach
        }
    }

    /**
     * DISPLAY LOGIC:
     * Füllt die UI-Elemente mit den Daten aus dem Movie-Objekt.
     */
    private fun displayMovieDetails(movie: Movie) {
        binding.tvDetailsTitle.text = movie.title
        binding.tvDetailsReleaseDate.text = "Release Date: ${movie.releaseDate}"
        
        // Null-Check für die Beschreibung: Falls keine vorhanden, Text anpassen
        if (movie.overview.isNullOrEmpty()) {
            binding.tvDetailsOverview.text = "No description available for this movie."
            binding.tvDetailsOverview.alpha = 0.5f // Transparenz senken
        } else {
            binding.tvDetailsOverview.text = movie.overview
            binding.tvDetailsOverview.alpha = 1.0f
        }

        /**
         * GLIDE: Lädt das Poster-Bild asynchron über die URL.
         */
        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        Glide.with(this)
            .load(posterUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(binding.ivDetailsPoster)
            
        /**
         * CLICK-LISTENER:
         * Hier entscheiden wir, unter welchem Status der Film in Firestore gespeichert wird.
         */
        binding.btnMarkAsSeen.setOnClickListener {
            saveMovieWithStatus(movie, "seen")
        }

        binding.btnAddToPlanned.setOnClickListener {
            saveMovieWithStatus(movie, "planned")
        }
    }

    /**
     * FIRESTORE-LOGIK:
     * Speichert die Filmdaten in der Cloud-Datenbank unter dem Profil des Users.
     */
    private fun saveMovieWithStatus(movie: Movie, status: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            /**
             * MAP (Hashmap): Wir erstellen ein Schlüssel-Wert-Paar-Objekt für Firebase.
             * Firebase Firestore speichert Daten im JSON-ähnlichen Format.
             */
            val movieData = hashMapOf(
                "id" to movie.id,
                "title" to movie.title,
                "posterPath" to movie.posterPath,
                "releaseDate" to movie.releaseDate,
                "overview" to movie.overview,
                "status" to status // "seen" (gesehen) oder "planned" (geplant)
            )

            /**
             * HIERARCHIE IN FIRESTORE:
             * users (Sammlung) -> [UID des Users] (Dokument) -> watchlist (Untersammlung) -> [MovieID] (Dokument)
             */
            db.collection("users")
                .document(currentUser.uid)
                .collection("watchlist")
                .document(movie.id.toString())
                .set(movieData) // .set überschreibt oder erstellt das Dokument
                .addOnSuccessListener {
                    val message = if (status == "seen") "Marked as seen" else "Added to planned"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    /**
     * API-LOGIK (Credits):
     * Holt zusätzliche Infos (Schauspieler/Regie), die nicht im Standard-Film-Objekt sind.
     */
    private fun loadCredits(movieId: Int) {
        TmdbClient.instance.getMovieCredits(movieId, API_KEY).enqueue(object : Callback<CreditsResponse> {
            override fun onResponse(call: Call<CreditsResponse>, response: Response<CreditsResponse>) {
                if (_binding != null && response.isSuccessful) {
                    val credits = response.body()
                    
                    // .find { ... }: Durchsucht die Crew-Liste nach dem ersten "Director"
                    val director = credits?.crew?.find { it.job == "Director" }?.name ?: "Unknown"
                    binding.tvDetailsDirector.text = "Director: $director"
                    
                    /**
                     * joinToString: Verwandelt eine Liste von Namen in einen einzigen String,
                     * getrennt durch Kommata. .take(5) nimmt nur die ersten 5 Personen.
                     */
                    val actors = credits?.cast?.take(5)?.joinToString(", ") { it.name } ?: "No info"
                    binding.tvDetailsActors.text = "Actors: $actors"
                }
            }

            override fun onFailure(call: Call<CreditsResponse>, t: Throwable) {
                if (_binding != null) {
                    binding.tvDetailsDirector.text = "Failed to load credits"
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
