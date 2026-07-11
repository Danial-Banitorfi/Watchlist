package com.example.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.watchlist.models.Movie

class MovieAdapter(
    private var movies: List<Movie>,
    private val onItemClick: (Movie) -> Unit
) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivPoster: ImageView = view.findViewById(R.id.iv_poster)
        val tvTitle: TextView = view.findViewById(R.id.tv_movie_title)
        val tvYear: TextView = view.findViewById(R.id.tv_release_year)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_movie, parent, false)
        return MovieViewHolder(view)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movies[position]
        holder.tvTitle.text = movie.title
        
        // Erscheinungsjahr extrahieren (z.B. "2024-05-01" -> "2024")
        val year = movie.releaseDate?.split("-")?.firstOrNull() ?: ""
        holder.tvYear.text = if (year.isNotEmpty()) "($year)" else ""

        // Bild mit Glide laden
        val posterUrl = "https://image.tmdb.org/t/p/w500${movie.posterPath}"
        Glide.with(holder.itemView.context)
            .load(posterUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.ivPoster)

        holder.itemView.setOnClickListener { onItemClick(movie) }
    }

    override fun getItemCount() = movies.size

    // Gibt den Film an einer bestimmten Position zurück (wichtig für das Löschen)
    fun getMovieAt(position: Int): Movie {
        return movies[position]
    }

    fun updateMovies(newMovies: List<Movie>) {
        movies = newMovies
        notifyDataSetChanged()
    }
}
