package com.example.watchlist.api

// Retrofit ist die Standard-Bibliothek für Android, um HTTP-Anfragen (Internet) zu machen
import retrofit2.Retrofit
// Der GSON-Converter hilft dabei, JSON-Antworten vom Server automatisch in Kotlin-Objekte umzuwandeln
import retrofit2.converter.gson.GsonConverterFactory

/**
 * OBJECT (Singleton-Pattern): 
 * In Kotlin erstellt "object" eine Klasse, von der es systemweit nur eine einzige Instanz gibt.
 * Das ist perfekt für einen API-Client, da wir nicht für jede Anfrage eine neue Verbindung
 * aufbauen wollen, sondern eine einzige nutzen.
 */
object TmdbClient {
    /**
     * CONST VAL: Ein konstanter Wert, der zur Kompilierzeit feststeht.
     * Die Basis-URL ist die Grundadresse der TMDB API.
     */
    private const val BASE_URL = "https://api.themoviedb.org/3/"

    /**
     * BY LAZY: Das ist ein "Lazy Delegate".
     * Die Variable "instance" wird nicht sofort beim App-Start erstellt, sondern erst in dem Moment,
     * wenn sie zum ersten Mal aufgerufen wird (z.B. im HomeFragment).
     * Das spart wertvolle Ressourcen (Arbeitsspeicher) beim Start der App.
     */
    val instance: TmdbApi by lazy {
        /**
         * Retrofit.Builder(): Hier nutzen wir das "Builder-Pattern", um unseren Client
         * Schritt für Schritt zu konfigurieren.
         */
        val retrofit = Retrofit.Builder()
            // Hier legen wir die Grundadresse fest
            .baseUrl(BASE_URL)
            /**
             * addConverterFactory: Ohne diesen Zusatz würde Retrofit nur "Text" empfangen.
             * GsonConverterFactory sorgt dafür, dass aus dem JSON-Text automatisch 
             * Movie-Objekte werden.
             */
            .addConverterFactory(GsonConverterFactory.create())
            // Schließt die Konfiguration ab und baut das Retrofit-Objekt
            .build()

        /**
         * .create(TmdbApi::class.java):
         * Hier geschieht die Magie. Retrofit nimmt unser Interface (TmdbApi) und 
         * erstellt im Hintergrund den Code, der die Internetanfragen wirklich ausführt.
         */
        retrofit.create(TmdbApi::class.java)
    }
}
