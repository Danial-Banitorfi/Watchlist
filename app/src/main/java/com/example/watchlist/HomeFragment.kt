package com.example.watchlist

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.watchlist.api.TmdbClient
import com.example.watchlist.databinding.FragmentHomeBinding
import com.example.watchlist.models.Movie
import com.example.watchlist.models.MovieResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// Hauptseite der App
class HomeFragment : Fragment(R.layout.fragment_home) {

    // Verbindung zum Layout (Binding)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // Dein TMDB API Key
    private val API_KEY = "c804cebbae0a6ec42aecbf76a22d7a77"
    
    // Der Adapter verwaltet die Liste der Filme
    private lateinit var movieAdapter: MovieAdapter
    
    companion object {
        // Statischer Speicher: Bleibt erhalten, solange die App offen ist
        private var moviesCache: List<Movie>? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Layout-Verbindung initialisieren
        _binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        setupSearchView()
        
        // Wenn man auf den Titel klickt, navigiert die App zur Watchlist-Seite
        binding.tvHomeTitle.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_WatchlistFragment)
        }
        
        // Filme nur laden, wenn wir noch keine haben (verhindert ständiges Reloading)
        if (moviesCache == null) {
            loadPopularMovies()
        } else {
            movieAdapter.updateMovies(moviesCache!!)
            binding.tvListTitle.text = "Vorschläge für dich"
        }
    }

    private fun setupRecyclerView() {
        // Die Liste soll die Elemente untereinander anzeigen
        binding.rvMovies.layoutManager = LinearLayoutManager(context)
        
        // Adapter erstellen und Klick-Logik definieren
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            // Ausgewählten Film in ein Paket (Bundle) packen
            val bundle = Bundle().apply {
                putSerializable("movie", movie)
            }
            // Zur Detailseite springen
            findNavController().navigate(R.id.action_HomeFragment_to_DetailsFragment, bundle)
        }
        binding.rvMovies.adapter = movieAdapter
    }

    private fun loadPopularMovies() {
        // Würfelt eine Zahl zwischen 1 und 50 für abwechslungsreiche Startseiten
        val randomPage = (1..50).random()

        // API-Anfrage starten
        TmdbClient.instance.getPopularMovies(API_KEY, page = randomPage).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (_binding != null && response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    moviesCache = movies // Filme im statischen Speicher ablegen
                    movieAdapter.updateMovies(movies)
                    // Titel der Liste aktualisieren
                    binding.tvListTitle.text = "Vorschläge für dich"
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (_binding != null) {
                    Toast.makeText(context, "Fehler: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupSearchView() {
        // Reagiert auf Eingaben in der Suchleiste
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchMovies(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Statt neu zu laden, nutzen wir die gespeicherten Filme (Optimierung)
                    moviesCache?.let {
                        movieAdapter.updateMovies(it)
                        binding.tvListTitle.text = "Vorschläge für dich"
                    } ?: loadPopularMovies()
                }
                return false
            }
        })
    }

    private fun searchMovies(query: String) {
        // Suche bei TMDB ausführen
        TmdbClient.instance.searchMovies(API_KEY, query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (_binding != null && response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    movieAdapter.updateMovies(movies)
                    binding.tvListTitle.text = "Ergebnisse für: $query"
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (_binding != null) {
                    Toast.makeText(context, "Suche fehlgeschlagen", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
