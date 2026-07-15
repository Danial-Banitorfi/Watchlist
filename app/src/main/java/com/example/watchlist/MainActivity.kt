package com.example.watchlist

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.watchlist.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

/**
 * MAIN ACTIVITY: Der Haupteinstiegspunkt und Container der App.
 * Da wir ein "Single Activity"-Design nutzen, dient diese Klasse als Rahmen
 * für alle anderen Fragmente (Login, Home, Details).
 */
class MainActivity : AppCompatActivity() {

    /**
     * LATEINIT: Verspricht dem Compiler, dass wir die Variable initialisieren,
     * bevor wir sie benutzen. Das verhindert null-checks überall im Code.
     */
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /**
         * VIEW BINDING: Ersetzt findViewById.
         * Wir "blasen" das Layout auf und können direkt über binding auf IDs zugreifen.
         */
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Die Toolbar (oben) als Action-Bar für die App setzen
        setSupportActionBar(binding.toolbar)

        /**
         * NAVIGATION SETUP:
         * Wir finden den NavHost (das leere Feld im Layout, in das Fragmente geladen werden)
         * und holen uns den NavController, der die Wechsel steuert.
         */
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        /**
         * APP BAR CONFIGURATION:
         * Definiert, welche Seiten als "Top-Level" gelten (kein Zurück-Pfeil).
         * Das Login (FirstFragment) und Home sollen keinen automatischen Zurück-Pfeil haben.
         */
        appBarConfiguration = AppBarConfiguration(setOf(R.id.FirstFragment, R.id.HomeFragment))
        setupActionBarWithNavController(navController, appBarConfiguration)

        /**
         * LISTENER (Zuhörer):
         * Jedes Mal, wenn wir die Seite wechseln (z.B. Login -> Home), wird dieser Block ausgeführt.
         * invalidateOptionsMenu() sorgt dafür, dass onCreateOptionsMenu() neu aufgerufen wird.
         */
        navController.addOnDestinationChangedListener { _, destination, _ ->
            invalidateOptionsMenu()
        }
    }

    /**
     * MENÜ ERSTELLEN:
     * Hier entscheiden wir, ob das Drei-Punkte-Menü (Logout) angezeigt wird.
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Logik: Das Menü soll NUR auf der Hauptseite (HomeFragment) sichtbar sein
        if (navController.currentDestination?.id == R.id.HomeFragment) {
            menuInflater.inflate(R.menu.menu_main, menu)
            return true
        }
        return false // Auf Login/Register wird kein Menü angezeigt
    }

    /**
     * MENÜ-KLICK REAKTION:
     * Was passiert, wenn man auf "Logout" drückt?
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // 1. User bei Firebase abmelden
                FirebaseAuth.getInstance().signOut()
                // 2. Zurück zum Login-Bildschirm (FirstFragment) springen
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.FirstFragment) // Nach dem Logout wird der Benutzer wieder zum Login-Bildschirm weitergeleitet.
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * ZURÜCK-NAVIGATION:
     * Steuert das Verhalten des Pfeils oben links in der Toolbar.
     */
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
