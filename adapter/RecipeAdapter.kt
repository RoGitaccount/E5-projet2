package fr.ro.recipemanager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import org.json.JSONObject

class RecipeAdapter(
    val context: Context,
    private val recettes: List<Recipe>,
    private val id_utilisateur: String?,
    private val itemClickListener: (Recipe) -> Unit
) : RecyclerView.Adapter<RecipeAdapter.RecetteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vertical_recipe, parent, false)
        return RecetteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetteViewHolder, position: Int) {
        val recette = recettes[position]
        holder.bind(context, recette, id_utilisateur, itemClickListener)
    }

    override fun getItemCount(): Int {
        return recettes.size
    }

    class RecetteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.Titre_recette)
        private val authorTextView: TextView = itemView.findViewById(R.id.author)
        private val dateTextView: TextView = itemView.findViewById(R.id.publication)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val starIcon: ImageView = itemView.findViewById(R.id.star_icon)

        fun bind(context: Context, recette: Recipe, id_utilisateur: String?, itemClickListener: (Recipe) -> Unit) {
            titleTextView.text = recette.titre
            authorTextView.text = "par : ${recette.prenom}"
            dateTextView.text = "publié le : ${recette.date_creation}"
            val imageUrl = "http://192.168.56.1/api/actions/images/" + recette.image
            Log.d("picasso", "Loading image URL: $imageUrl")
            Picasso.get()
                .load(imageUrl)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .into(imageView, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
                        Log.d("Picasso", "Image loaded successfully")
                    }

                    override fun onError(e: Exception?) {
                        Log.e("Picasso", "Error loading image", e)
                        imageView.setImageResource(R.drawable.img_defaut)
                    }
                })

            // Set the star icon based on whether the recipe is a favorite
            updateStarIcon(recette.isFavorite)

            starIcon.setOnClickListener {
                recette.isFavorite = !recette.isFavorite
                updateStarIcon(recette.isFavorite)
                if (recette.isFavorite) {
                    addRecipeToFavorites(context, recette, id_utilisateur)
                } else {
                    removeRecipeFromFavorites(context, recette, id_utilisateur)
                }
            }

            itemView.setOnClickListener { itemClickListener(recette) }
        }

        private fun updateStarIcon(isFavorite: Boolean) {
            if (isFavorite) {
                starIcon.setImageResource(R.drawable.ic_star)
            } else {
                starIcon.setImageResource(R.drawable.ic_unstar)
            }
        }

        private fun addRecipeToFavorites(context: Context, recipe: Recipe, id_utilisateur: String?) {
            val url = "http://192.168.56.1/api/actions/addFavorite.php"
            val json = JSONObject().apply {
                put("id_recette", recipe.id_recette)
                put("id_utilisateur", id_utilisateur)
            }

            val request = JsonObjectRequest(
                Request.Method.POST, url, json,
                Response.Listener { response ->
                    Log.d("RecipeAdapter", "Recipe added to favorites successfully")
                },
                Response.ErrorListener { error ->
                    Log.e("RecipeAdapter", "Failed to add recipe to favorites", error)
                    val errorMessage = error.message ?: "Erreur inconnue lors de l'ajout aux favoris"
                    Toast.makeText(context, "Erreur: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(context).add(request)
        }

        private fun removeRecipeFromFavorites(context: Context, recipe: Recipe, id_utilisateur: String?) {
            val url = "http://192.168.56.1/api/actions/removeFavorite.php"
            val json = JSONObject().apply {
                put("id_recette", recipe.id_recette)
                put("id_utilisateur", id_utilisateur)
            }

            val request = JsonObjectRequest(
                Request.Method.POST, url, json,
                Response.Listener { response ->
                    Log.d("RecipeAdapter", "Recipe removed from favorites successfully")
                },
                Response.ErrorListener { error ->
                    Log.e("RecipeAdapter", "Failed to remove recipe from favorites", error)
                    val errorMessage = error.message ?: "Erreur inconnue lors de la suppression des favoris"
                    Toast.makeText(context, "Erreur: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(context).add(request)
        }
    }
}
