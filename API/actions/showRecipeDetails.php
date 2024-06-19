<?php
// Inclure le fichier DatabaseManager.php pour accéder à la classe DatabaseManager
require_once __DIR__.'/../config/DatabaseManager.php';

// Créer une instance de DatabaseManager
$databaseManager = new DatabaseManager();

// Vérifier si l'identifiant de l'utilisateur est fourni dans la requête GET
$id_utilisateur = isset($_GET['id_utilisateur']) ? intval($_GET['id_utilisateur']) : null;

// Vérifier si l'identifiant de l'utilisateur est présent
if ($id_utilisateur === null) {
    // Si l'identifiant de l'utilisateur n'est pas fourni, vous devez gérer cette situation en conséquence
    // Par exemple, vous pouvez renvoyer une erreur 400 Bad Request
    http_response_code(400);
    echo json_encode(array("success" => false, "error" => "User ID is required"), JSON_UNESCAPED_UNICODE);
    exit;
}

// Utiliser l'identifiant de l'utilisateur pour récupérer les recettes
$recettes = $databaseManager->showAllRecettes($id_utilisateur);

// Vérifier si la récupération des recettes a réussi
if ($recettes !== false) {
    // Préparer un tableau pour stocker les données des recettes
    $recettesData = array();

    // Parcourir toutes les recettes et les ajouter au tableau
    foreach ($recettes as $recette) {
        // Ajouter les détails de la recette au tableau
        $recettesData[] = array(
            'id_recette' => intval($recette['id_recette']),
            'titre' => strip_tags($recette['titre']),
            'image' => strip_tags($recette['image']),
            'prenom' => strip_tags($recette["prenom"]),
            'ingredients' => strip_tags($recette["ingredients"]),
            'etapes_preparation' => strip_tags($recette["etapes_preparation"]),
            'temps_preparation' => strip_tags($recette["temps_preparation"]),
            'temps_cuisson' => strip_tags($recette["temps_cuisson"]),
            'date_creation' => $recette["date_creation"],
            'description' => strip_tags($recette['description']),
            'isFavorite' => $recette['isFavorite']
        );
    }

    // Retourner les recettes au format JSON
    echo json_encode(array("success" => true, "recettes" => $recettesData), JSON_UNESCAPED_UNICODE);
} else {
    // En cas d'échec de la récupération des recettes
    echo json_encode(array("success" => false, "error" => "Failed to fetch recipes."), JSON_UNESCAPED_UNICODE);
}
?>
