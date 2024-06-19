<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$jsondata = file_get_contents('php://input');
$data = json_decode($jsondata, true);
$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

if ($method == 'POST') {
    if (!isset($data['email']) || !isset($data['mot_de_passe']) || empty(trim($data['email'])) || empty(trim($data['mot_de_passe']))) {        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "Veuillez remplir tous les champs"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $email = strip_tags($data['email']);
    $password = $data['mot_de_passe'];
    $user = $databaseManager->connectUser($email, $password);

    if ($user !== false) {
        // Vérification du rôle de l'utilisateur
        if ($user['Role'] !== 'admin') {
            http_response_code(403); // Forbidden
            echo json_encode(array("success" => false, "error" => "Seuls les utilisateurs ayant des droits d'administration peuvent se connecter"), JSON_UNESCAPED_UNICODE);
            exit;
        }

        // Utilisateur authentifié avec succès et rôle admin

        // Exclure le champ "mot_de_passe" de l'utilisateur de la réponse JSON
        unset($user['mot_de_passe']);

        // Stocker les informations de l'utilisateur dans la session
        $_SESSION["user"] = $user;

        // Réponse JSON indiquant une connexion réussie
        http_response_code(200); // OK
        echo json_encode(array("success" => true, "user" => $user), JSON_UNESCAPED_UNICODE);
    } else {
        // Échec de l'authentification de l'utilisateur
        http_response_code(401); // Unauthorized
        echo json_encode(array("success" => false, "error" => "Les informations renseignées sont incorrectes"), JSON_UNESCAPED_UNICODE);
    }

} else {
    // Méthode non autorisée
    http_response_code(405); // Method Not Allowed
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>