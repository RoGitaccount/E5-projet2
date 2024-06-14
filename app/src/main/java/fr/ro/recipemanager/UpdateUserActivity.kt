package fr.ro.recipemanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class UpdateUserActivity : AppCompatActivity() {
    private lateinit var lastNameEditText: EditText
    private lateinit var firstNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var roleSpinner: Spinner
    private lateinit var confirmButton: Button
    private lateinit var returnButton: ImageView
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_update_user)

        lastNameEditText = findViewById(R.id.update_LastNameEditText)
        firstNameEditText = findViewById(R.id.update_NameEditText)
        emailEditText = findViewById(R.id.update_EmailEditText)
        roleSpinner = findViewById(R.id.update_RoleEditText)
        confirmButton = findViewById(R.id.update_user_confirm_button)
        returnButton = findViewById(R.id.return_button)



        userId = intent.getIntExtra("id_utilisateur", -1)

        val lastName = intent.getStringExtra("user_last_name")
        val firstName = intent.getStringExtra("user_first_name")
        val email = intent.getStringExtra("user_email")
        val role = intent.getStringExtra("user_role")

        lastNameEditText.setText(lastName)
        firstNameEditText.setText(firstName)
        emailEditText.setText(email)

        val rolesArray = resources.getStringArray(R.array.Role)
        val rolePosition = rolesArray.indexOf(role)
        if (rolePosition >= 0) {
            roleSpinner.setSelection(rolePosition)
        }

        confirmButton.setOnClickListener {
            // Logique pour mettre à jour l'utilisateur
            updateUser()
        }

        returnButton.setOnClickListener {
            val intent = Intent(this, UsersActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun updateUser() {
        val lastName = lastNameEditText.text.toString()
        val firstName = firstNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val role = roleSpinner.selectedItem.toString()



        val userData = JSONObject().apply {
            put("id_utilisateur",userId )
            put("nom", lastName)
            put("prenom", firstName)
            put("email", email)
            put("Role", role)
        }

        val url = "http://192.168.56.1/api/actions/updateUser.php"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.POST, url, userData, // Ensure PUT request is used
            { response ->
                Toast.makeText(this, "Utilisateur mis à jour avec succès!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, UsersActivity::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                Toast.makeText(this, "Erreur: le mail n'est pas valide", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}

