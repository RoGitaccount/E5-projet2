<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$jsondata = file_get_contents('php://input');
$data = json_decode($jsondata, true);
$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

// Vérifiez si la méthode est POST ou PUT
if ($method == 'POST' || $method == 'PUT') {
    if (!isset($data['id_utilisateur'])) {
        http_response_code(400);
        echo json_encode(array("success" => false, "error" => "L'ID de l'utilisateur est requis"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $id_utilisateur = strip_tags($data['id_utilisateur']);

    // Récupérer l'utilisateur existant
    $existing_user = $databaseManager->getUserById($id_utilisateur);
    if (!$existing_user) {
        http_response_code(404);
        echo json_encode(array("success" => false, "error" => "L'utilisateur spécifié n'existe pas"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Utiliser les nouvelles valeurs ou les valeurs existantes si les nouvelles ne sont pas fournies ou sont vides ou contiennent seulement des espaces
    $nom = (!empty(trim($data['nom']))) ? strip_tags($data['nom']) : $existing_user['nom'];
    $prenom = (!empty(trim($data['prenom']))) ? strip_tags($data['prenom']) : $existing_user['prenom'];
    $email = (!empty(trim($data['email']))) ? strip_tags($data['email']) : $existing_user['email'];
    $role = (!empty(trim($data['Role']))) ? strip_tags($data['Role']) : $existing_user['Role'];

    // Vérification de la validité de l'email
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400);
        echo json_encode(array("success" => false, "error" => "L'email fourni n'est pas valide"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $result = $databaseManager->updateUser($id_utilisateur, $nom, $prenom, $email, $role);

    if ($result) {
        http_response_code(200);
        echo json_encode(array("success" => true, "message" => "Utilisateur mis à jour avec succès"), JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(400); // Bad Request, car la mise à jour a échoué en raison d'un email déjà existant
        echo json_encode(array("success" => false, "error" => "L'email existe déjà"), JSON_UNESCAPED_UNICODE);
    }
} else {
    http_response_code(405);
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
