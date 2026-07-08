package com.example.watchlist.api

import com.example.watchlist.models.CreditsResponse
import com.example.watchlist.models.MovieResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    // Holt populäre Filme für die Startseite mit Seitenzahl
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "de-DE",
        @Query("page") page: Int // Neu: Erlaubt das Laden verschiedener Seiten
    ): Call<MovieResponse>

    // Suchfunktion
    @GET("search/movie")
    fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "de-DE"
    ): Call<MovieResponse>

    // Holt Details wie Schauspieler und Regisseur
    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<CreditsResponse>
}
