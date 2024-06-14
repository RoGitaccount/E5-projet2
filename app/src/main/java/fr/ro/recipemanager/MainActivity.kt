package fr.ro.recipemanager

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var EmailEditText: EditText
    private lateinit var PasswordEditText: EditText
    private lateinit var connectBtn: Button
    private lateinit var createAccountBtn: TextView
    private lateinit var errorConnectAccountTextView: TextView
    private var email: String? = null
    private var mot_de_passe: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        EmailEditText = findViewById(R.id.EmailEditText)
        PasswordEditText = findViewById(R.id.PasswordEditText)
        connectBtn = findViewById(R.id.connectBtn)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        errorConnectAccountTextView = findViewById(R.id.errorConnectAccountTextView)

        connectBtn.setOnClickListener {
            email = EmailEditText.text.toString()
            mot_de_passe = PasswordEditText.text.toString()
            connectUser(email!!, mot_de_passe!!)
        }

        createAccountBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun onApiResponse(response: JSONObject) {
        try {
            val success = response.getBoolean("success")
            if (success) {
                val user = response.getJSONObject("user")
                val id_utilisateur = user.getString("id_utilisateur")
                val prenom = user.getString("prenom")

                val intent = Intent(this@MainActivity, Accueil::class.java)
                intent.putExtra("id_utilisateur", id_utilisateur)
                intent.putExtra("email", email)
                intent.putExtra("prenom", prenom)
                startActivity(intent)
                finish()
            } else {
                val error = response.getString("error")
                showError(error)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            showError("Erreur lors du traitement de la réponse")
        }
    }

    private fun connectUser(email: String, mot_de_passe: String) {
        val url = "http://192.168.56.1/api/actions/connectAccount.php"
        val params = HashMap<String, String>()
        params["email"] = email
        params["mot_de_passe"] = mot_de_passe
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
            { response ->
                onApiResponse(response)
                Toast.makeText(this@MainActivity, "Connexion réussie", Toast.LENGTH_SHORT).show()
            },
            { error ->
                val response = error.networkResponse
                if (response != null && response.data != null) {
                    val errorMessage = String(response.data)
                    try {
                        val errorJson = JSONObject(errorMessage)
                        val errorMsg = errorJson.getString("error")
                        showError(errorMsg)
                    } catch (e: JSONException) {
                        showError("Erreur inconnue lors de la connexion")
                    }
                }
            }
        )
        // Initialisez la file de requêtes si nécessaire
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun showError(message: String) {
        errorConnectAccountTextView.text = message
        errorConnectAccountTextView.visibility = View.VISIBLE
    }
}
