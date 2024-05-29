package fr.ro.recipemanager

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class RecipeAdapter(private val recettes: List<Recipe>) : RecyclerView.Adapter<RecipeAdapter.RecetteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecetteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_vertical_recipe, parent, false)
        return RecetteViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecetteViewHolder, position: Int) {
        val recette = recettes[position]
        holder.bind(recette)
    }

    override fun getItemCount(): Int {
        return recettes.size
    }

    class RecetteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.Titre_recette)
        private val authorTextView: TextView = itemView.findViewById(R.id.author)
        private val dateTextView: TextView = itemView.findViewById(R.id.publication)
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)


        fun bind(recette: Recipe) {
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
                        // Optionally, set a placeholder or error image
                        imageView.setImageResource(R.drawable.img_defaut)
                    }
                })
        }

    }
}
