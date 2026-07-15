package com.example.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.watchlist.models.Movie

/**
 * ADAPTER-KLASSE: Der Adapter ist der "Vermittler". 
 * Er nimmt eine Liste von Daten (movies) und sagt der RecyclerView, 
 * wie diese Daten in den einzelnen Zeilen (Items) angezeigt werden sollen.
 */
class MovieAdapter(
    private var movies: List<Movie>, // Die Datenquelle: Eine Liste von Film-Objekten
    private val onItemClick: (Movie) -> Unit // Ein Lambda-Ausdruck für Klicks (Navigation)
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    /**
     * VIEWHOLDER-KLASSE: Ein Container für die UI-Elemente einer einzelnen Zeile.
     * Er "hält" die Views im Speicher fest, damit sie nicht jedes Mal neu gesucht werden müssen.
     */
    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPoster: ImageView = view.findViewById(R.id.iv_poster)
        val tvTitle: TextView = view.findViewById(R.id.tv_movie_title)
        val tvYear: TextView = view.findViewById(R.id.tv_release_year)
    }

    /**
     * ONCREATEVIEWHOLDER: Wird aufgerufen, wenn die RecyclerView ein neues Zeilen-Layout braucht.
     * Hier "blasen" (inflate) wir das XML-Layout 'item_movie' auf.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    /**
     * ONBINDVIEWHOLDER: Verknüpft die echten Daten mit der View.
     * Diese Methode wird aufgerufen, wenn ein Film-Item sichtbar wird.
     */
    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position] // Welcher Film ist an dieser Listenposition?
        holder.tvTitle.text = movie.title
        
        /**
         * Logik: Das Datum von TMDB kommt als "YYYY-MM-DD".
         * Wir splitten am Bindestrich und nehmen den ersten Teil (das Jahr).
         */
        val year = movie.releaseDate?.split("-")?.firstOrNull() ?: ""
        holder.tvYear.text = if (year.isNotEmpty()) "($year)" else ""

        /**
         * BILDER LADEN (Glide): 
         * TMDB liefert nur Teil-Pfade (z.B. /abc.jpg). Wir setzen die Basis-URL davor.
         */
        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .placeholder(android.R.drawable.ic_menu_gallery) // Standardbild während des Ladens
            .into(holder.ivPoster)

        // Klick-Event: Wenn man auf das ganze Item drückt, wird die Funktion oben ausgeführt
        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    /**
     * GETITEMCOUNT: Sagt der Liste, wie viele Einträge sie insgesamt hat.
     */
    override fun getItemCount() = movies.size

    /**
     * Hilfsfunktion: Gibt das Movie-Objekt an einer bestimmten Stelle zurück.
     * Wichtig für das Löschen per Wischgeste (Swipe-to-Delete).
     */
    fun getMovieAt(position: Int): Movie {
        return movies[position]
    }

    /**
     * UPDATEMOVIES: Aktualisiert die Liste der Filme (z.B. nach einer Suche).
     * notifyDataSetChanged() sagt dem UI: "Alles neu zeichnen!"
     */
    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
