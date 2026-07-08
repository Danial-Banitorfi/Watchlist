package com.example.watchlist.api

// Wir importieren Retrofit, das Tool für Internetabfragen
import retrofit2.Retrofit
// Wir importieren den Converter, der JSON-Text in Kotlin-Objekte verwandelt
import retrofit2.converter.gson.GsonConverterFactory

// "object" bedeutet: Es gibt nur eine einzige Instanz (Singleton) in der ganzen App
object TmdbClient {
    // Die Basis-URL, unter der alle TMDB-Abfragen starten
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    // "lazy" bedeutet: Das Telefon wird erst vorbereitet, wenn wir es zum ersten Mal brauchen
    val instance: TmdbApi by lazy {
        // Wir bauen das Retrofit-Objekt zusammen
        val retrofit = Retrofit.Builder()
            // Wir sagen ihm, wo er im Internet suchen soll
            .baseUrl(BASE_URL)
            // Wir fügen GSON hinzu, damit er den JSON-Salat vom Server automatisch versteht
            .addConverterFactory(GsonConverterFactory.create())
            // Jetzt wird das Ganze fertig gebaut
            .build()

        // Hier verbinden wir unser "Telefon" (Retrofit) mit unserer "Nummernliste" (TmdbApi)
        retrofit.create(TmdbApi::class.java)
    }
}
