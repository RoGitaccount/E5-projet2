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

class FavoriteAdapter(
    val context: Context,
    private val favoriteRecipes: MutableList<Recipe>, // Changer favoriteRecipes en MutableList
    private val id_utilisateur: String?,
    private val itemClickListener: (Recipe) -> Unit
) : RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vertical_recipe, parent, false)
        return FavoriteViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        val recipe = favoriteRecipes[position]
        holder.bind(context, recipe, id_utilisateur, this, itemClickListener)
    }

    override fun getItemCount(): Int {
        return favoriteRecipes.size
    }

    fun removeFavoriteRecipe(recipe: Recipe) {
        favoriteRecipes.remove(recipe)
        notifyDataSetChanged()
    }

    class FavoriteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.Titre_recette)
        private val authorTextView: TextView = itemView.findViewById(R.id.author)
        private val dateTextView: TextView = itemView.findViewById(R.id.publication)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val starIcon: ImageView = itemView.findViewById(R.id.star_icon)

        fun bind(context: Context, recipe: Recipe, id_utilisateur: String?, adapter: FavoriteAdapter, itemClickListener: (Recipe) -> Unit) {
            titleTextView.text = recipe.titre
            authorTextView.text = "par : ${recipe.prenom}"
            dateTextView.text = "publié le : ${recipe.date_creation}"
            val imageUrl = "http://192.168.56.1/api/actions/images/" + recipe.image
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
            updateStarIcon(recipe.isFavorite)

            starIcon.setOnClickListener {
                recipe.isFavorite = !recipe.isFavorite
                updateStarIcon(recipe.isFavorite)
                if (recipe.isFavorite) {
                    // If needed, handle adding recipe to favorites
                } else {
                    removeRecipeFromFavorites(context, recipe, id_utilisateur, adapter) // Ajouter l'adaptateur en paramètre
                }
            }

            itemView.setOnClickListener { itemClickListener(recipe) }
        }

        private fun updateStarIcon(isFavorite: Boolean) {
            if (isFavorite) {
                starIcon.setImageResource(R.drawable.ic_star)
            } else {
                starIcon.setImageResource(R.drawable.ic_unstar)
            }
        }

        private fun removeRecipeFromFavorites(context: Context, recipe: Recipe, id_utilisateur: String?, adapter: FavoriteAdapter) {
            val url = "http://192.168.56.1/api/actions/removeFavorite.php"
            val json = JSONObject().apply {
                put("id_recette", recipe.id_recette)
                put("id_utilisateur", id_utilisateur)
            }

            val request = JsonObjectRequest(
                Request.Method.POST, url, json,
                Response.Listener { response ->
                    Log.d("FavoriteAdapter", "Recipe removed from favorites successfully")
                    // Rafraîchir la liste des favoris après la suppression de la recette
                    adapter.removeFavoriteRecipe(recipe)
                },
                Response.ErrorListener { error ->
                    Log.e("FavoriteAdapter", "Failed to remove recipe from favorites", error)
                    val errorMessage = error.message ?: "Erreur inconnue lors de la suppression des favoris"
                    Toast.makeText(context, "Erreur: $errorMessage", Toast.LENGTH_SHORT).show()
                }
            )

            Volley.newRequestQueue(context).add(request)
        }
    }
}
