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

class RecipesFragment : Fragment() {

    private lateinit var recettesRecyclerView: RecyclerView
    private lateinit var recetteAdapter: RecipeAdapter
    private val recettes = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recipes, container, false)

        recettesRecyclerView = view.findViewById(R.id.Recipes_recycler_list)
        recetteAdapter = RecipeAdapter(recettes)
        recettesRecyclerView.adapter = recetteAdapter
        recettesRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchRecettes()

        return view
    }

    private fun fetchRecettes() {
        val url = "http://192.168.56.1/api/actions/showRecipe.php"
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val recettesArray = response.getJSONArray("recettes")
                        for (i in 0 until recettesArray.length()) {
                            val recetteJson = recettesArray.getJSONObject(i)
                            val recette = Recipe(
                                recetteJson.getString("id_recette"),
                                recetteJson.getString("titre"),
                                recetteJson.getString("date_creation"),
                                recetteJson.getString("prenom"),
                                recetteJson.getString("image")
                            )
                            recettes.add(recette)
                        }
                        recetteAdapter.notifyDataSetChanged()
                    } else {
                        val errorMessage = response.getString("error")
                        Log.e("RecipeFragment", "Erreur lors de la récupération des recettes: $errorMessage")
                        Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    val errorMessage = e.message ?: "Erreur inconnue lors de la récupération des recettes"
                    Log.e("RecipeFragment", "Erreur lors de la récupération des recettes: $errorMessage", e)
                    Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                val errorMessage = error.message ?: "Erreur inconnue lors de la récupération des recettes"
                Log.e("RecipeFragment", "Erreur lors de la récupération des recettes: $errorMessage", error)
                Toast.makeText(requireContext(), "Erreur: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )

        val requestQueue = Volley.newRequestQueue(requireContext())
        requestQueue.add(jsonObjectRequest)
    }
}
