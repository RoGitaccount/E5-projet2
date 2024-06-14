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
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException
import org.json.JSONObject

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var nameEditText: EditText
    private lateinit var lastnameEditText: EditText
    private lateinit var createAccountBtn: Button
    private lateinit var alreadyHasAccountTextView: TextView
    private lateinit var errorCreateAccountTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        emailEditText = findViewById(R.id.createEmailEditText)
        passwordEditText = findViewById(R.id.createPasswordEditText)
        nameEditText = findViewById(R.id.createNameEditText)
        lastnameEditText = findViewById(R.id.createLastNameEditText)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        alreadyHasAccountTextView = findViewById(R.id.alreadyHasAccountBtn)
        errorCreateAccountTextView = findViewById(R.id.errorCreateAccountTextView)

        createAccountBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val nom = nameEditText.text.toString()
            val prenom = lastnameEditText.text.toString()

            createAccount(email, password, nom, prenom)
        }

        alreadyHasAccountTextView.setOnClickListener {
            val mainActivity = Intent(this, MainActivity::class.java)
            startActivity(mainActivity)
        }
    }

    private fun createAccount(email: String, password: String, nom: String, prenom: String) {
        val url = "http://192.168.56.1/api/actions/createAccount.php"

        val params = HashMap<String, String>()
        params["email"] = email
        params["mot_de_passe"] = password
        params["nom"] = nom
        params["prenom"] = prenom

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, JSONObject(params as Map<*, *>?),
            Response.Listener { response ->
                onApiResponse(response)
            },
            Response.ErrorListener { error ->
                val response = error.networkResponse
                if (response != null && response.data != null) {
                    val errorMessage = String(response.data)
                    try {
                        val errorJson = JSONObject(errorMessage)
                        val errorMsg = errorJson.getString("error")
                        showError(errorMsg)
                    } catch (e: JSONException) {
                        // Ignorer les erreurs de parsing
                    }
                }
            }
        )
        // Initialisez la file de requêtes si nécessaire
        val requestQueue = Volley.newRequestQueue(this)
        requestQueue.add(jsonObjectRequest)
    }

    private fun onApiResponse(response: JSONObject) {
        try {
            val success = response.getBoolean("success")
            if (success) {
                // Afficher le Toast
                Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show()

                // Rediriger vers MainActivity
                val intent = Intent(this@CreateAccountActivity, MainActivity::class.java)
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

    private fun showError(message: String) {
        errorCreateAccountTextView.text = message
        errorCreateAccountTextView.visibility = View.VISIBLE
    }
}
