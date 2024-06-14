package fr.ro.recipemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class UsersActivity : AppCompatActivity() {

    private lateinit var welcomeTextView: TextView
    private lateinit var logoutButton: ImageButton
    private var prenom: String? = null
    private var id_utilisateur: String? = null
    private lateinit var userAdapter: UserAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_acceuil)

        welcomeTextView = findViewById(R.id.WelcomeTextView)
        logoutButton = findViewById(R.id.logoutBtn)

        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        prenom = intent.getStringExtra("prenom") ?: sharedPreferences.getString("prenom", null)
        id_utilisateur = intent.getStringExtra("id_utilisateur") ?: sharedPreferences.getString("id_utilisateur", null)

        val editor = sharedPreferences.edit()
        editor.putString("prenom", prenom)
        editor.putString("id_utilisateur", id_utilisateur)
        editor.apply()

        welcomeTextView.text = "Bienvenue: $prenom"

        logoutButton.setOnClickListener {
            clearUserSession()
            val intent = Intent(this@UsersActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, UsersFragment())
                .commit()
        }

        val navigationView: BottomNavigationView = findViewById(R.id.navigation_view)
        navigationView.selectedItemId = R.id.user_page

        navigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home_page -> {
                    val intent = Intent(this, Accueil::class.java)
                    startActivity(intent)
                    true
                }
                R.id.user_page -> true
                R.id.add_rcp_page -> {
                    val intent = Intent(this, AddRecipeActivity::class.java)
                    intent.putExtra("id_utilisateur", id_utilisateur)
                    startActivity(intent)
                    true
                }
                R.id.fav_page -> {
                    // Navigate to favorites page
                    val intent = Intent(this, FavoritesActivity::class.java)
                    intent.putExtra("id_utilisateur", id_utilisateur)
                    startActivity(intent)
                    true
                }
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

    fun refreshUserList() {
        val usersFragment = UsersFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, usersFragment)
            .commit()
    }
}
