<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

if ($method == 'POST' || $method == 'GET') {
    if (!isset($_REQUEST['id_recette'])) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "ID de recette manquant"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $id_recette = $_REQUEST['id_recette'];

    // Appel de la méthode pour supprimer la recette
    $recipeDeleted = $databaseManager->deleteRecipe($id_recette);

    if ($recipeDeleted) {
        // Suppression réussie
        http_response_code(200); // OK
        echo json_encode(array("success" => true, "message" => "Recette supprimée avec succès"), JSON_UNESCAPED_UNICODE);
    } else {
        // Échec de la suppression
        http_response_code(500); // Internal Server Error
        echo json_encode(array("success" => false, "error" => "Cette ID n'existe pas"), JSON_UNESCAPED_UNICODE);
    }
} else {
    // Méthode non autorisée
    http_response_code(405); // Method Not Allowed
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
