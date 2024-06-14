package fr.ro.recipemanager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException



class FavoriteFragment : Fragment() {

    private lateinit var favoritesRecyclerView: RecyclerView
    private lateinit var favoriteAdapter: FavoriteAdapter
    private val favoriteRecipes = mutableListOf<Recipe>()
    private var id_utilisateur: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_favorites, container, false)
        favoritesRecyclerView = view.findViewById(R.id.Favorites_recycler_list)
        id_utilisateur = arguments?.getString("id_utilisateur")

        favoriteAdapter = FavoriteAdapter(
            requireContext(),
            favoriteRecipes,
            id_utilisateur,)
        { recipe -> showRecipePopup(recipe) }

        favoritesRecyclerView.adapter = favoriteAdapter
        favoritesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchFavoriteRecipes()

        return view
    }

    fun fetchFavoriteRecipes() {
        val url = "http://192.168.56.1/api/actions/showfavRecipe.php?id_auteur=$id_utilisateur"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val recipesArray = response.getJSONArray("recettes_favorites")
                        favoriteRecipes.clear()
                        for (i in 0 until recipesArray.length()) {
                            val recipeJson = recipesArray.getJSONObject(i)
                            val recipe = Recipe(
                                recipeJson.getString("id_recette"),
                                recipeJson.getString("titre"),
                                recipeJson.getString("date_creation"),
                                recipeJson.getString("prenom"),
                                recipeJson.optString("image", "img_defaut"),
                                recipeJson.optString("description", "Aucune description"),
                                recipeJson.optString("ingredients", "Ingrédients non disponibles"),
                                recipeJson.optString(
                                    "etapes_preparation",
                                    "Étapes non disponibles"
                                ),
                                recipeJson.optString(
                                    "temps_preparation",
                                    "Temps de préparation non disponible"
                                ),
                                recipeJson.optString(
                                    "temps_cuisson",
                                    "Temps de cuisson non disponible"
                                ),
                                true
                            )
                            favoriteRecipes.add(recipe)
                        }
                        favoriteAdapter.notifyDataSetChanged()
                    } else {
                        val errorMessage = response.getString("error")
                        Log.e(
                            "FavoritesActivity",
                            "Erreur lors de la récupération des recettes favorites: $errorMessage"
                        )
                        Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_LONG).show()
                    }

                } catch (e: JSONException) {
                    e.printStackTrace()
                    val errorMessage = e.message
                        ?: "Erreur inconnue lors de la récupération des recettes favorites"
                    Log.e(
                        "FavoritesActivity",
                        "Erreur lors de la récupération des recettes favorites: $errorMessage", e)
                    Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                val errorMessage = error.message
                    ?: "Erreur inconnue lors de la récupération des recettes favorites"
                Log.e(
                    "FavoritesActivity",
                    "Erreur lors de la récupération des recettes favorites: $errorMessage",
                    error
                )
                Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonObjectRequest)
    }

    private fun showRecipePopup(recipe: Recipe) {
        val recipePopup = RecipePopup(requireContext(), recipe) {
            fetchFavoriteRecipes() // Actualiser la liste lorsque la recette est retirée des favoris
        }
        recipePopup.show()
    }
}
