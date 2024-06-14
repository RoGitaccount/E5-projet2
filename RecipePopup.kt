package fr.ro.recipemanager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import java.io.Serializable

class RecipePopup(
    context: Context,
    private val recipe: Recipe,
    private val onRecipeDeleted: () -> Unit // callback function
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.popup_recipe)

        val imageView: ImageView = findViewById(R.id.image_item)
        val nameTextView: TextView = findViewById(R.id.popup_rcp_name)
        val descriptionTextView: TextView = findViewById(R.id.popup_rcp_description)
        val ingredientsTextView: TextView = findViewById(R.id.popup_rcp_ingredients)
        val stepsTextView: TextView = findViewById(R.id.popup_rcp_steps)
        val preparationTimeTextView: TextView = findViewById(R.id.popup_rcp_time_steps_title)
        val cookingTimeTextView: TextView = findViewById(R.id.popup_rcp_time_cuisson_title)

        Picasso.get()
            .load("http://192.168.56.1/api/actions/images/" + recipe.image)
            .into(imageView)

        nameTextView.text = recipe.titre
        descriptionTextView.text = recipe.description
        ingredientsTextView.text = recipe.ingredients
        stepsTextView.text = recipe.etapes_preparation
        preparationTimeTextView.text = recipe.temps_preparation
        cookingTimeTextView.text = recipe.temps_cuisson

        findViewById<ImageView>(R.id.close_button).setOnClickListener {
            dismiss()
        }
        findViewById<ImageView>(R.id.delete_button).setOnClickListener {
            deleteRecipe(recipe.id_recette)
        }

        // Ajouter un OnClickListener pour le bouton de modification
        findViewById<ImageView>(R.id.rcp_edit_button).setOnClickListener {
            // Envoyer la recette à modifier à une nouvelle activité d'édition
            val editIntent = Intent(context, UpdateRecipeActivity::class.java)
            editIntent.putExtra("recipe", recipe as Serializable)
            context.startActivity(editIntent)
            dismiss() // Dismiss the popup after starting the edit activity
        }
    }

    private fun deleteRecipe(idRecette: String) {
        val queue = Volley.newRequestQueue(context)
        val url = "http://192.168.56.1/api/actions/deleteRecipe.php?id_recette=$idRecette"

        val stringRequest = StringRequest(
            Request.Method.GET, url,
            Response.Listener<String> { response ->
                // Handle the response
                Toast.makeText(context, "Recette supprimée avec succès", Toast.LENGTH_SHORT).show()
                dismiss()
                onRecipeDeleted()  // Call the callback to notify that the recipe has been deleted
            },
            Response.ErrorListener {
                // Handle the error
                Toast.makeText(context, "Erreur de suppression de la recette", Toast.LENGTH_SHORT).show()
            })

        queue.add(stringRequest)
    }
}
