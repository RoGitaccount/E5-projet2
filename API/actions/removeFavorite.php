<?php
// Inclure le fichier DatabaseManager.php pour accéder à la classe DatabaseManager
require_once __DIR__.'/../config/DatabaseManager.php';

// Créer une instance de DatabaseManager
$databaseManager = new DatabaseManager();

// Vérifier la méthode de la requête HTTP
if ($_SERVER["REQUEST_METHOD"] === "POST") {

    // Vérifier si les données requises sont présentes dans la requête JSON
    $jsondata = file_get_contents('php://input');
    $data = json_decode($jsondata, true);

    if (!isset($data['id_recette'])) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "ID recette is required"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Utiliser une requête préparée pour éviter les injections SQL
    $id_recette = intval($data['id_recette']);
    $id_utilisateur = intval($data['id_utilisateur']);
    //$id_utilisateur = intval($_SESSION["user"]["id_utilisateur"]);

    // Retirer la recette des favoris
    if ($databaseManager->retirerRecetteDesFavoris($id_recette, $id_utilisateur)) {
        http_response_code(200); // OK
        echo json_encode(array("success" => true, "message" => "Recipe removed from favorites"), JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(500); // Internal Server Error
        echo json_encode(array("success" => false, "error" => "Failed to remove recipe from favorites"), JSON_UNESCAPED_UNICODE);
    }
}
?>
