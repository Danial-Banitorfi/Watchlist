package com.example.watchlist

// Wir holen uns die nötigen Werkzeuge für UI, Listen und Datenbank
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchlist.databinding.FragmentWatchlistBinding
import com.example.watchlist.models.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// Die Klasse für unsere Listen-Ansicht
class WatchlistFragment : Fragment(R.layout.fragment_watchlist) {

    // Verbindung zum XML-Layout (Binding)
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    // Zugriff auf die Cloud-Datenbank und den angemeldeten User
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Wir brauchen zwei Adapter: Einen für die linke Liste, einen für die rechte
    private lateinit var seenAdapter: MovieAdapter
    private lateinit var plannedAdapter: MovieAdapter

    // Wird aufgerufen, sobald die Seite bereit ist
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Das Binding-Objekt mit dem echten Layout verknüpfen
        _binding = FragmentWatchlistBinding.bind(view)

        // Listen vorbereiten (Wie sollen sie aussehen?)
        setupRecyclerViews()
        
        // Daten aus dem Internet laden
        loadWatchlistData()
    }

    private fun setupRecyclerViews() {
        // --- LINKE LISTE (Gesehen) ---
        // LinearLayoutManager sorgt dafür, dass die Filme untereinander stehen
        binding.rvSeen.layoutManager = LinearLayoutManager(context)
        // Wir erstellen den Adapter. Wenn man auf einen Film klickt, öffnen wir die Details.
        seenAdapter = MovieAdapter(emptyList()) { movie ->
            openDetails(movie)
        }
        binding.rvSeen.adapter = seenAdapter

        // --- RECHTE LISTE (Geplant) ---
        binding.rvPlanned.layoutManager = LinearLayoutManager(context)
        plannedAdapter = MovieAdapter(emptyList()) { movie ->
            openDetails(movie)
        }
        binding.rvPlanned.adapter = plannedAdapter
    }

    private fun loadWatchlistData() {
        // Die ID des Users holen (Wer will seine Liste sehen?)
        val userId = auth.currentUser?.uid ?: return

        // --- ABFRAGE FÜR "GESEHEN" ---
        // Gehe zum Ordner users -> [ID] -> watchlist
        db.collection("users").document(userId).collection("watchlist")
            .whereEqualTo("status", "seen") // Filter: Nur Filme mit Status "seen"
            .addSnapshotListener { value, error ->
                // "Snapshot" bedeutet: Wenn sich in der Datenbank was ändert, 
                // wird die App sofort automatisch aktualisiert!
                if (_binding == null || error != null) return@addSnapshotListener
                
                // Wir verwandeln die Datenbank-Dokumente in eine Liste von Movie-Objekten
                val movies = value?.documents?.mapNotNull { it.toObject(Movie::class.java) } ?: emptyList()
                seenAdapter.updateMovies(movies) // Liste im UI aktualisieren
            }

        // --- ABFRAGE FÜR "GEPLANT" ---
        db.collection("users").document(userId).collection("watchlist")
            .whereEqualTo("status", "planned") // Filter: Nur Filme mit Status "planned"
            .addSnapshotListener { value, error ->
                if (_binding == null || error != null) return@addSnapshotListener
                
                val movies = value?.documents?.mapNotNull { it.toObject(Movie::class.java) } ?: emptyList()
                plannedAdapter.updateMovies(movies)
            }
    }

    // Hilfsfunktion: Öffnet die Detailseite für den angeklickten Film
    private fun openDetails(movie: Movie) {
        // Wir packen den Film in ein Paket (Bundle)
        val bundle = Bundle().apply {
            // putSerializable erlaubt es uns, das ganze Film-Objekt zu verschicken
            putSerializable("movie", movie)
        }
        // Wir nutzen die neue Verbindung (Action), die wir im nav_graph angelegt haben
        findNavController().navigate(R.id.action_WatchlistFragment_to_DetailsFragment, bundle)
    }

    // Speicher freigeben, wenn die Seite verlassen wird
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
