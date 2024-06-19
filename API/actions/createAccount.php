<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

$databaseManager = new DatabaseManager();

$jsondata = file_get_contents('php://input');
$data = json_decode($jsondata, true);
$method = $_SERVER['REQUEST_METHOD'];

header('Content-Type: application/json');

if ($method == 'POST') {
    // Vérifier que les champs sont définis et non vides
    if (!isset($data['nom']) || empty($data['nom']) ||
        !isset($data['prenom']) || empty($data['prenom']) ||
        !isset($data['email']) || empty($data['email']) ||
        !isset($data['mot_de_passe']) || empty($data['mot_de_passe'])) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "Veuillez remplir tous les champs"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $nom = strip_tags($data['nom']);
    $prenom = strip_tags($data['prenom']);
    $email = strip_tags($data['email']);
    $password = $data['mot_de_passe'];

    // Vérification de l'adresse e-mail
    if (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "L'adresse e-mail fournie n'est pas valide"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Vérification du mot de passe
    if (strlen($password) < 6 || !preg_match("/[0-9]/", $password) || !preg_match("/[§!@#$%^&*()\-_=+{};:,.<>]/", $password)) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "Le mot de passe doit contenir plus de 6 caractères, un chiffre et un caractère spécial."), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Appel de la méthode pour créer un nouvel utilisateur
    $userId = $databaseManager->createUser($nom, $prenom, $email, $password);

    if ($userId) {
        // Nouvel utilisateur créé avec succès
        http_response_code(201); // Created
        echo json_encode(array("success" => true, "message" => "Création de l'utilisateur réussie.", "id_utilisateur" => $userId, "prenom" => $prenom), JSON_UNESCAPED_UNICODE);
    } else {
        // Échec de la création de compte (adresse e-mail déjà utilisée)
        http_response_code(409); // Conflict
        echo json_encode(array("success" => false, "error" => "Cette adresse e-mail est déjà utilisée. Veuillez en choisir une autre."), JSON_UNESCAPED_UNICODE);
    }
} else {
    // Méthode non autorisée
    http_response_code(405); // Method Not Allowed
    echo json_encode(array("success" => false, "error" => "Méthode de requête non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
