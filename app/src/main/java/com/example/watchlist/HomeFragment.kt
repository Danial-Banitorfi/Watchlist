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

/**
 * HOME FRAGMENT: Die Hauptseite der App, auf der populäre Filme angezeigt werden.
 * Erbt von Fragment(R.layout.fragment_home), was das Layout direkt im Konstruktor festlegt.
 */
class HomeFragment : Fragment(R.layout.fragment_home) {

    /**
     * VIEW BINDING in Fragmenten:
     * _binding ist nullable, da die View eines Fragments zerstört werden kann, 
     * während das Fragment-Objekt noch lebt. 'binding' ist ein praktischer Getter,
     * der uns die !!-Null-Prüfung erspart.
     */
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    // API_KEY: Der persönliche Zugangsschlüssel für die The Movie Database API
    private val API_KEY = "c804cebbae0a6ec42aecbf76a22d7a77"
    
    // LATEINIT: Der Adapter wird erst in onViewCreated initialisiert
    private lateinit var movieAdapter: MovieAdapter
    
    /**
     * COMPANION OBJECT: Statische Variablen in Kotlin.
     * moviesCache speichert die Filme, damit sie beim Hin- und Her-Navigieren
     * nicht jedes Mal neu aus dem Internet geladen werden müssen (spart Datenvolumen).
     */
    companion object {
        private var moviesCache: List<Movie>? = null
    }

    /**
     * ONVIEWCREATED: Wird aufgerufen, sobald das UI-Layout fertig gezeichnet wurde.
     * Hier fangen wir an, die Logik mit den Buttons und Listen zu verbinden.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Verknüpft das Layout-XML mit dieser Klasse
        _binding = FragmentHomeBinding.bind(view)

        // Setup-Funktionen aufrufen (Strukturierung des Codes)
        setupRecyclerView()
        setupSearchView()
        
        // Klick auf den Titel führt zur Watchlist
        binding.tvHomeTitle.setOnClickListener {
            findNavController().navigate(R.id.action_HomeFragment_to_WatchlistFragment)
        }
        
        // CACHE-LOGIK: Wenn wir schon Filme geladen haben, nimm die. Sonst: API-Anfrage.
        if (moviesCache == null) {
            loadPopularMovies()
        } else {
            movieAdapter.updateMovies(moviesCache!!)
        }
    }

    /**
     * RECYCLERVIEW SETUP:
     * Konfiguriert die Liste und sagt ihr, was beim Klick auf einen Film passieren soll.
     */
    private fun setupRecyclerView() {
        // LinearLayoutManager sorgt dafür, dass die Filme untereinander stehen (wie eine Liste)
        binding.rvMovies.layoutManager = LinearLayoutManager(context)
        
        /**
         * LAMBDA-AUSDRUCK: { movie -> ... }
         * Das ist die Logik, die ausgeführt wird, wenn der User im Adapter auf einen Film klickt.
         */
        movieAdapter = MovieAdapter(emptyList()) { movie ->
            // BUNDLE: Ein "Paket", um Daten zwischen Fragmenten zu verschicken
            val bundle = Bundle().apply {
                putSerializable("movie", movie) // Der Film muss "Serializable" sein (siehe Movie.kt)
            }
            // NAVIGATION: Springe zum DetailsFragment und nimm das Paket mit
            findNavController().navigate(R.id.action_HomeFragment_to_DetailsFragment, bundle)
        }
        binding.rvMovies.adapter = movieAdapter
    }

    /**
     * API-ANFRAGE: Populäre Filme laden.
     */
    private fun loadPopularMovies() {
        // Zufällige Seite zwischen 1 und 50, damit die App nicht immer gleich aussieht
        val randomPage = (1..50).random()

        /**
         * ENQUEUE: Startet die Internetanfrage asynchron im Hintergrund.
         * Würden wir sie im Vordergrund (Main Thread) machen, würde die App einfrieren.
         */
        TmdbClient.instance.getPopularMovies(API_KEY, page = randomPage).enqueue(object : Callback<MovieResponse> {
            /**
             * ONRESPONSE: Wird aufgerufen, wenn der Server geantwortet hat.
             */
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                // Nur weitermachen, wenn das Fragment noch existiert (_binding != null)
                if (_binding != null && response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    moviesCache = movies // In den Cache speichern
                    movieAdapter.updateMovies(movies) // Die Liste auf dem Bildschirm aktualisieren
                }
            }

            /**
             * ONFAILURE: Wird aufgerufen bei Verbindungsfehlern (z.B. kein Internet).
             */
            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (_binding != null) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * SUCHFUNKTION SETUP:
     * Überwacht die Eingaben in der Suchzeile oben.
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            // Wird aufgerufen, wenn der User die "Enter"-Taste drückt
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchMovies(query)
                }
                return true
            }

            // Wird bei jedem Tastendruck aufgerufen
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    // Wenn die Suche leer ist, zeigen wir wieder die populären Filme aus dem Cache
                    moviesCache?.let {
                        movieAdapter.updateMovies(it)
                    } ?: loadPopularMovies()
                }
                return false
            }
        })
    }

    /**
     * FILMSUCHE ÜBER DIE API:
     * Ähnlich wie loadPopularMovies, nutzt aber den Such-Endpunkt.
     */
    private fun searchMovies(query: String) {
        TmdbClient.instance.searchMovies(API_KEY, query).enqueue(object : Callback<MovieResponse> {
            override fun onResponse(call: Call<MovieResponse>, response: Response<MovieResponse>) {
                if (_binding != null && response.isSuccessful) {
                    val movies = response.body()?.results ?: emptyList()
                    movieAdapter.updateMovies(movies)
                    binding.tvListTitle.text = "Results for: $query"
                }
            }

            override fun onFailure(call: Call<MovieResponse>, t: Throwable) {
                if (_binding != null) {
                    Toast.makeText(context, "Search failed", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * LEBENSZYKLUS-REINIGUNG:
     * Verhindert Memory Leaks (Speicherlecks), indem die Binding-Referenz gelöscht wird.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
