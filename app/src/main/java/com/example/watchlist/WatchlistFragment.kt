package com.example.watchlist

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.watchlist.databinding.FragmentWatchlistBinding
import com.example.watchlist.models.Movie
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * WATCHLIST FRAGMENT:
 * Zeigt die gespeicherten Filme des Benutzers an, unterteilt in "Gesehen" (Seen) 
 * und "Geplant" (Planned). Nutzt Firebase Firestore für Echtzeit-Updates.
 */
class WatchlistFragment : Fragment(R.layout.fragment_watchlist) {

    // View Binding für den Zugriff auf die Layout-Elemente
    private var _binding: FragmentWatchlistBinding? = null
    private val binding get() = _binding!!

    // Firebase-Instanzen für Datenbank und Authentifizierung
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * ZWEI ADAPTER: 
     * Da wir zwei getrennte Listen (RecyclerViews) haben, benötigen wir auch
     * zwei Instanzen des MovieAdapters.
     */
    private lateinit var seenAdapter: MovieAdapter
    private lateinit var plannedAdapter: MovieAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWatchlistBinding.bind(view)

        // Konfiguration der Listenansichten
        setupRecyclerViews()
        
        // Laden der Daten aus der Cloud
        loadWatchlistData()
    }

    /**
     * SETUP RECYCLERVIEWS:
     * Initialisiert beide Listen und fügt die "Swipe-to-Delete" (Wischen zum Löschen) Funktion hinzu.
     */
    private fun setupRecyclerViews() {
        // --- SEEN LIST (Gesehen) ---
        binding.rvSeen.layoutManager = LinearLayoutManager(context)
        seenAdapter = MovieAdapter(emptyList()) { movie -> openDetails(movie) }
        binding.rvSeen.adapter = seenAdapter
        setupSwipeToDelete(binding.rvSeen, seenAdapter)

        // --- PLANNED LIST (Geplant) ---
        binding.rvPlanned.layoutManager = LinearLayoutManager(context)
        plannedAdapter = MovieAdapter(emptyList()) { movie -> openDetails(movie) }
        binding.rvPlanned.adapter = plannedAdapter
        setupSwipeToDelete(binding.rvPlanned, plannedAdapter)
    }

    /**
     * SWIPE TO DELETE:
     * Ermöglicht es dem User, ein Item nach links zu wischen, um es zu löschen.
     */
    private fun setupSwipeToDelete(recyclerView: RecyclerView, adapter: MovieAdapter) {
        val swipeHandler = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false

            // Wird aufgerufen, wenn die Wisch-Geste abgeschlossen ist
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val movie = adapter.getMovieAt(position)
                deleteMovieFromFirestore(movie.id)
            }

            // Zeichnet den roten Hintergrund während des Wischens
            override fun onChildDraw(c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, state: Int, isActive: Boolean) {
                val background = ColorDrawable(Color.RED)
                background.setBounds(vh.itemView.right + dX.toInt(), vh.itemView.top, vh.itemView.right, vh.itemView.bottom)
                background.draw(c)
                super.onChildDraw(c, rv, vh, dX, dY, state, isActive)
            }
        }
        ItemTouchHelper(swipeHandler).attachToRecyclerView(recyclerView)
    }

    /**
     * LÖSCH-LOGIK:
     * Entfernt das Dokument des Films aus der Firestore-Sammlung des Users.
     */
    private fun deleteMovieFromFirestore(movieId: Int) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId).collection("watchlist")
            .document(movieId.toString())
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Movie removed", Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * FIRESTORE ABFRAGE:
     * Lädt die Filme des Users und filtert sie nach ihrem Status.
     */
    private fun loadWatchlistData() {
        val userId = auth.currentUser?.uid ?: return

        /**
         * addSnapshotListener:
         * Das ist die "Echtzeit-Verbindung". Sobald ein Film in der DB gelöscht oder 
         * hinzugefügt wird, reagiert diese Methode sofort, ohne dass der User neu laden muss.
         */
        
        // Abfrage für Filme mit Status "seen"
        db.collection("users").document(userId).collection("watchlist")
            .whereEqualTo("status", "seen")
            .addSnapshotListener { value, error ->
                if (_binding == null || error != null) return@addSnapshotListener
                
                // .toObject: Wandelt das Firebase-Dokument automatisch in unsere Movie-Klasse um
                val movies = value?.documents?.mapNotNull { it.toObject(Movie::class.java) } ?: emptyList()
                seenAdapter.updateMovies(movies)
            }

        // Abfrage für Filme mit Status "planned"
        db.collection("users").document(userId).collection("watchlist")
            .whereEqualTo("status", "planned")
            .addSnapshotListener { value, error ->
                if (_binding == null || error != null) return@addSnapshotListener
                
                val movies = value?.documents?.mapNotNull { it.toObject(Movie::class.java) } ?: emptyList()
                plannedAdapter.updateMovies(movies)
            }
    }

    /**
     * NAVIGATION:
     * Öffnet die Details für einen Film aus der Watchlist.
     */
    private fun openDetails(movie: Movie) {
        val bundle = Bundle().apply {
            putSerializable("movie", movie)
        }
        findNavController().navigate(R.id.action_WatchlistFragment_to_DetailsFragment, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
