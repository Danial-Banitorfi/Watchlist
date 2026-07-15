package com.example.watchlist.models

// GSON wird benötigt, um die Namen in der API (JSON) auf unsere Kotlin-Variablen zu mappen
import com.google.gson.annotations.SerializedName
// Serializable erlaubt es, dieses Objekt als Ganzes zwischen Fragmenten zu verschicken
import java.io.Serializable

/**
 * DATA CLASS: Ein spezieller Kotlin-Typ, der hauptsächlich zum Speichern von Daten gedacht ist.
 * Er erstellt automatisch Funktionen wie equals(), hashCode() und toString().
 *
 * SERIALIZABLE: Dieses Interface ist wichtig für die Navigation. Es erlaubt uns,
 * ein komplettes Movie-Objekt von einem Fragment zum nächsten zu übergeben.
 */
data class Movie(
    // Standardwerte (z.B. = 0) sorgen dafür, dass wir ein leeres Objekt erstellen können
    val id: Int = 0,
    val title: String = "",
    
    /**
     * @SerializedName: Die API liefert uns "overview". In Kotlin könnten wir die Variable
     * auch anders nennen, aber hiermit schlagen wir die Brücke zwischen JSON und Code.
     */
    @SerializedName("overview") 
    val overview: String = "",
    
    /**
     * String?: Das Fragezeichen bedeutet, dass dieser Wert "null" sein darf.
     * Das ist wichtig, da manche Filme bei TMDB vielleicht noch kein Poster oder Datum haben.
     */
    @SerializedName("poster_path")
    val posterPath: String? = null,
    
    @SerializedName("release_date")
    val releaseDate: String? = null,
    
    // Status für die Watchlist (geplant, gesehen, etc.)
    val status: String = "planned"
) : Serializable

/**
 * Hilfsklasse für die API-Antwort.
 * Wenn wir "popular movies" anfragen, schickt uns TMDB nicht direkt eine Liste,
 * sondern ein Objekt, das die Liste in einem Feld namens "results" enthält.
 */
data class MovieResponse(
    val results: List<Movie> = emptyList()
)

/**
 * Modell für die Credits eines Films (Schauspieler und Crew).
 */
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
