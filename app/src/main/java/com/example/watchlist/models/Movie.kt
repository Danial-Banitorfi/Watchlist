package com.example.watchlist.models

// Wir importieren die SerializedName-Funktion, um Internet-Daten (JSON) zu übersetzen
import com.google.gson.annotations.SerializedName
import java.io.Serializable

// "data class" ist ein Bauplan für Datenobjekte. 
// Jedes Feld hat einen Standardwert (z.B. = 0 oder = ""), damit Firebase es lesen kann.
data class Movie(
    val id: Int = 0, // Die eindeutige Nummer des Films von TMDB
    val title: String = "", // Der Name des Films
    @SerializedName("overview") val description: String = "", // Die Handlung (Heißt bei TMDB "overview")
    @SerializedName("poster_path") val posterPath: String? = null, // Der Link zum Bild
    @SerializedName("release_date") val releaseDate: String? = null, // Das Datum
    val status: String = "planned" // Neu: Speichert, ob der Film "seen" (gesehen) oder "planned" (geplant) ist
) : Serializable // Macht das Objekt verschickbar zwischen Seiten

// Die Antwort von der TMDB-Suche (enthält eine Liste von Filmen)
data class MovieResponse(
    val results: List<Movie> = emptyList()
)

// Bauplan für die Mitwirkenden (Schauspieler und Crew)
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
