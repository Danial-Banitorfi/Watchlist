package com.example.watchlist.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

// Der saubere Bauplan für einen Film
data class Movie(
    val id: Int = 0,
    val title: String = "",
    
    // Die Beschreibung heißt jetzt überall "overview"
    @SerializedName("overview") 
    val overview: String = "",
    
    @SerializedName("poster_path")
    val posterPath: String? = null,
    
    @SerializedName("release_date")
    val releaseDate: String? = null,
    
    val status: String = "planned"
) : Serializable

data class MovieResponse(
    val results: List<Movie> = emptyList()
)

data class CreditsResponse(
    val cast: List<CastMember> = emptyList(),
    val crew: List<CrewMember> = emptyList()
)

data class CastMember(
    val name: String = "",
    val character: String = ""
)

data class CrewMember(
    val name: String = "",
    val job: String = ""
)
