package fr.ro.recipemanager

import java.io.Serializable

data class Recipe(
    val id_recette: String,
    val titre: String, // Titre de la recette
    val date_creation: String, // Date de publication
    val prenom: String, // Auteur de la recette
    val image: String, // URL de l'image de la recette
    val description: String, // Description de la recette
    val ingredients: String, // Ingrédients de la recette
    val etapes_preparation: String, // Étapes de préparation de la recette
    val temps_preparation: String, // Temps de préparation
    val temps_cuisson: String, // Temps de cuisson
    var isFavorite: Boolean = false // Indicateur si la recette est favorite
): Serializable
