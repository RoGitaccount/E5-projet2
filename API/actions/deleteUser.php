<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

if ($method == 'POST' || $method == 'GET') {
    if (!isset($_REQUEST['id_utilisateur'])) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "ID de l'utilisateur manquant"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $id_utilisateur = $_REQUEST['id_utilisateur'];

    // Vérifier si l'utilisateur existe
    $userExists = $databaseManager->getUserById($id_utilisateur);

    if (!$userExists) {
        http_response_code(404); // Not Found
        echo json_encode(array("success" => false, "error" => "L'utilisateur avec cet ID n'existe pas"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Appel de la méthode pour supprimer l'utilisateur
    $userDeleted = $databaseManager->deleteUser($id_utilisateur);

    if ($userDeleted) {
        // Suppression réussie
        http_response_code(200); // OK
        echo json_encode(array("success" => true, "message" => "Utilisateur supprimé avec succès"), JSON_UNESCAPED_UNICODE);
    } else {
        // Échec de la suppression
        http_response_code(500); // Internal Server Error
        echo json_encode(array("success" => false, "error" => "Une erreur est survenue lors de la suppression de l'utilisateur"), JSON_UNESCAPED_UNICODE);
    }
} else {
    // Méthode non autorisée
    http_response_code(405); // Method Not Allowed
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
