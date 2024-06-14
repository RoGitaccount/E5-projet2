package fr.ro.recipemanager

import android.app.Activity
import android.content.Intent
import android.net.Uri
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
import com.squareup.picasso.Picasso
import org.json.JSONObject

class UpdateRecipeActivity : AppCompatActivity() {

    private lateinit var previewImage: ImageView
    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var ingredientsInput: EditText
    private lateinit var stepsInput: EditText
    private lateinit var prepaSpinner: Spinner
    private lateinit var cuissonSpinner: Spinner
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null
    private lateinit var recipe: Recipe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_update_recipe)

        previewImage = findViewById(R.id.update_preview_image)
        titleInput = findViewById(R.id.update_titre_input)
        descriptionInput = findViewById(R.id.update_description_input)
        ingredientsInput = findViewById(R.id.update_ingredient_input)
        stepsInput = findViewById(R.id.update_etapes_input)
        prepaSpinner = findViewById(R.id.update_prepa_spinner)
        cuissonSpinner = findViewById(R.id.update_cuisson_spinner)
        val returnButton = findViewById<ImageView>(R.id.rcp_return_button)
        val uploadButton: Button = findViewById(R.id.update_upload_button)
        val confirmButton: Button = findViewById(R.id.update_confirm_button)

        // Retrieve the recipe object from the intent
        recipe = intent.getSerializableExtra("recipe") as Recipe

        // Populate the input fields with the recipe data
        titleInput.setText(recipe.titre)
        descriptionInput.setText(recipe.description)
        ingredientsInput.setText(recipe.ingredients)
        stepsInput.setText(recipe.etapes_preparation)

        // Set spinner values accordingly
        val preparationArray = resources.getStringArray(R.array.temps_preparation)
        val preparationPosition = preparationArray.indexOf(recipe.temps_preparation)
        if (preparationPosition >= 0) {
            prepaSpinner.setSelection(preparationPosition)
        }

        val cookingArray = resources.getStringArray(R.array.temps_cuisson)
        val cookingPosition = cookingArray.indexOf(recipe.temps_cuisson)
        if (cookingPosition >= 0) {
            cuissonSpinner.setSelection(cookingPosition)
        }

        val imageUrl = "http://192.168.56.1/api/actions/images/" + recipe.image
        Picasso.get().load(imageUrl).into(previewImage)

        uploadButton.setOnClickListener {
            openImageChooser()
        }

        confirmButton.setOnClickListener {
            updateRecipe()
        }

        returnButton.setOnClickListener {
            val intent = Intent(this, Accueil::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/jpg", "image/png"))
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            previewImage.setImageURI(imageUri)
        }
    }

    private fun updateRecipe() {
        val titre = titleInput.text.toString()
        val description = descriptionInput.text.toString()
        val ingredients = ingredientsInput.text.toString()
        val etapesPreparation = stepsInput.text.toString()
        val tempsPreparation = prepaSpinner.selectedItem.toString()
        val tempsCuisson = cuissonSpinner.selectedItem.toString()


        // Convertir l'image en base64
        val imageBase64 = if (imageUri != null) {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            val byteArray = inputStream?.readBytes()
            if (byteArray != null) {
                android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT)
            } else {
                ""
            }
        } else {
            "" // Utiliser une chaîne vide si aucune image n'est fournie
        }

        val recipeData = JSONObject().apply {
            put("id_recette", recipe.id_recette) // Ensure this matches the successful request
            put("titre", titre)
            put("description", description)
            put("ingredients", ingredients)
            put("etapes_preparation", etapesPreparation)
            put("temps_preparation", tempsPreparation)
            put("temps_cuisson", tempsCuisson)
            put("image", imageBase64)
        }

        // Log the JSON payload
        //println("Recipe Data: $recipeData")

        val url = "http://192.168.56.1/api/actions/updateRecipe.php"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.PUT, url, recipeData, // Ensure PUT request is used
            { response ->
                Toast.makeText(this, "Recette mise à jour avec succès!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, Accueil::class.java)
                startActivity(intent)
                finish()
            },
            { error ->
                Toast.makeText(this, "Erreur: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )

        Volley.newRequestQueue(this).add(jsonObjectRequest)
    }
}
