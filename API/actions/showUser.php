<?php
// Inclure le fichier DatabaseManager.php pour accéder à la classe DatabaseManager
require_once __DIR__.'/../config/DatabaseManager.php';

// Créer une instance de DatabaseManager
$databaseManager = new DatabaseManager();

// Vérifier la méthode de la requête HTTP
if ($_SERVER["REQUEST_METHOD"] === "POST") {
    // Vérifier si l'utilisateur est connecté
    if (!isset($_SESSION["user"])) {
        http_response_code(401); // Unauthorized
        echo json_encode(array("success" => false, "error" => "User not authenticated"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Vérifier si les données requises sont présentes dans la requête JSON
    $jsondata = file_get_contents('php://input');
    $data = json_decode($jsondata, true);

    if (!isset($data['id_utilisateur'])) {
        http_response_code(400); // Bad Request
        echo json_encode(array("success" => false, "error" => "ID utilisateur is required"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Utiliser une requête préparée pour éviter les injections SQL
    $id_utilisateur = intval($data['id_utilisateur']);

    // Récupérer les détails de l'utilisateur
    $utilisateur = $databaseManager->showOneUser($id_utilisateur);

    if ($utilisateur !== false) {
        // Retourner les détails de l'utilisateur au format JSON
        echo json_encode(array("success" => true, "utilisateur" => $utilisateur), JSON_UNESCAPED_UNICODE);
    } else {
        // En cas d'échec de la récupération des détails de l'utilisateur
        echo json_encode(array("success" => false, "error" => "Failed to fetch user details."), JSON_UNESCAPED_UNICODE);
    }
} else {
    // Récupérer tous les utilisateurs en utilisant showAllUsers()
    $utilisateurs = $databaseManager->showAllUsers();

    // Vérifier si la récupération des utilisateurs a réussi
    if ($utilisateurs !== false) {
        // Préparer un tableau pour stocker les données des utilisateurs
        $utilisateursData = array();

        // Parcourir tous les utilisateurs et les ajouter au tableau
        foreach ($utilisateurs as $utilisateur) {
            // Ajouter les détails de l'utilisateur au tableau
            $utilisateursData[] = array(
                'id_utilisateur' => intval($utilisateur['id_utilisateur']),
                'nom' => strip_tags($utilisateur['nom']),
                'prenom' => strip_tags($utilisateur['prenom']),
                'email' => strip_tags($utilisateur['email']),
                'Role' => strip_tags($utilisateur['Role'])
            );
        }

        // Retourner les utilisateurs au format JSON
        echo json_encode(array("success" => true, "utilisateurs" => $utilisateursData), JSON_UNESCAPED_UNICODE);
    } else {
        // En cas d'échec de la récupération des utilisateurs
        echo json_encode(array("success" => false, "error" => "Failed to fetch users."), JSON_UNESCAPED_UNICODE);
    }
}
?>