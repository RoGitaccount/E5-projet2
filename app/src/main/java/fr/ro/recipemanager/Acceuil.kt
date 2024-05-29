package fr.ro.recipemanager

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class Acceuil : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: ImageButton
    private var prenom: String? = null
    private var id_utilisateur: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceuil)

        welcomeTextView = findViewById(R.id.WelcomeTextView)
        logoutButton = findViewById(R.id.logoutBtn)

        // Récupérer le prénom et l'id_utilisateur de l'intention ou des SharedPreferences
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prenom = intent.getStringExtra("prenom") ?: sharedPreferences.getString("prenom", null)
        id_utilisateur = intent.getStringExtra("id_utilisateur") ?: sharedPreferences.getString("id_utilisateur", null)

        // Stocker le prénom et l'id_utilisateur dans les SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("prenom", prenom)
        editor.putString("id_utilisateur", id_utilisateur)
        editor.apply()

        welcomeTextView.text = "Bienvenue: $prenom"

        logoutButton.setOnClickListener {
            clearUserSession()

            val intent = Intent(this@Acceuil, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RecipesFragment())
                .commit()
        }

        // Configuration du BottomNavigationView
        val navigationView: BottomNavigationView = findViewById(R.id.navigation_view)
        navigationView.selectedItemId = R.id.home_page

        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_page -> {
                    // Déjà sur Acceuil, ne rien faire
                    true
                }
                R.id.add_rcp_page -> {
                    val intent = Intent(this, AddRecipeActivity::class.java)
                    intent.putExtra("id_utilisateur", id_utilisateur)
                    startActivity(intent)
                    true
                }
                R.id.user_page -> {
                    val intent = Intent(this, UsersActivity::class.java)
                    intent.putExtra("id_utilisateur", id_utilisateur)
                    intent.putExtra("prenom", prenom)
                    startActivity(intent)
                    true
                }
                //R.id.fav_page -> {
                //  // Navigate to favorites page
                //val intent = Intent(this, FavoritesActivity::class.java)
                //startActivity(intent)
                //true
                //}
                else -> false
            }
        }
    }

    private fun clearUserSession() {
        val sharedPreferences: SharedPreferences =
            getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}
