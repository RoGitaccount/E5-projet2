<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

if ($method == 'GET') {
    // Vérifier si l'ID_auteur est passé en paramètre et s'il est valide
    if (!isset($_GET["id_auteur"]) || empty($_GET["id_auteur"]) || intval($_GET["id_auteur"]) <= 0) {
        // Retourner un message d'erreur
        echo json_encode(array("success" => false, "error" => "Cet utilisateur n'existe pas"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $id_utilisateur = intval($_GET["id_auteur"]);

    // Appel de la fonction pour récupérer les recettes favorites de l'utilisateur connecté
    $recettes_favorites = $databaseManager->showfavRecipe($id_utilisateur);

    // Retourner les recettes favorites au format JSON
    echo json_encode(array("success" => true, "recettes_favorites" => $recettes_favorites), JSON_UNESCAPED_UNICODE);
} else {
    // Méthode non autorisée
    http_response_code(405); // Method Not Allowed
    echo json_encode(array("success" => false, "error" => "Méthode de requête non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
