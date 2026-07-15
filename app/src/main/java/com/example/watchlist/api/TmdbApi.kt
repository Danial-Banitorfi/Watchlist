package com.example.watchlist.api

// Importe für die Datenmodelle, die wir als Antwort erwarten
import com.example.watchlist.models.CreditsResponse
import com.example.watchlist.models.MovieResponse
// Retrofit-Klassen für die Definition der HTTP-Anfragen
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * INTERFACE: In Retrofit definieren wir die API-Endpunkte als Interface.
 * Wir schreiben hier nur die "Verträge", die eigentliche Umsetzung (Implementation)
 * übernimmt Retrofit für uns im Hintergrund.
 */
interface TmdbApi {

    /**
     * @GET: Sagt Retrofit, dass wir eine HTTP GET-Anfrage senden wollen.
     * "movie/popular" ist der Pfad am Ende der Basis-URL.
     *
     * @Query: Diese Parameter werden an die URL angehängt (z.B. ?api_key=xyz&language=de).
     * Call<MovieResponse>: Das ist der Rückgabetyp. Wir erwarten ein Paket vom Typ MovieResponse.
     */
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int // Ermöglicht das "Paging" (Umblättern der Ergebnisse)
    ): Call<MovieResponse>

    /**
     * @GET("search/movie"): Endpunkt für die Filmsuche.
     * @Query("query"): Der Suchbegriff, den der User eingegeben hat.
     */
    @GET("search/movie")
    fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "en-US"
    ): Call<MovieResponse>

    /**
     * @Path: Wird genutzt, wenn ein Wert direkt Teil der URL-Struktur ist.
     * {movie_id} ist ein Platzhalter in der URL, der durch die Variable "movieId" ersetzt wird.
     * Holt die Credits (Schauspieler/Regie) für einen spezifischen Film.
     */
    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String
    ): Call<CreditsResponse>
}
