package fr.ro.recipemanager

data class Recipe(
    val id_recette: String,
    val titre: String, // Titre de la recette
    val date_creation: String, // Date de publication
    val prenom: String, // Auteur de la recette
    val image: String // URL de l'image de la recette
)


//var isFavorite: Boolean // Indicateur si la recette est favorite

