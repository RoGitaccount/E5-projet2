<?php
session_start();
require_once __DIR__.'/../config/DatabaseManager.php';

// Instancie l'objet DatabaseManager pour interagir avec la base de données
$databaseManager = new DatabaseManager();

// Récupère les données JSON envoyées dans la requête
$jsondata = file_get_contents('php://input');
$data = json_decode($jsondata, true);

// Récupère la méthode HTTP utilisée pour la requête (GET, POST, ...)
$method = $_SERVER['REQUEST_METHOD'];

// Définit le type de contenu de la réponse en JSON
header('Content-Type: application/json');

if ($method == 'POST') {
    // Vérifie que tous les champs nécessaires sont présents dans les données envoyées
    if (!isset($data['titre']) || !isset($data['description']) || !isset($data['ingredients']) || !isset($data['etapes_preparation']) || !isset($data['temps_preparation']) || !isset($data['temps_cuisson']) || !isset($data['image']) || !isset($data['id_auteur'])) {
        http_response_code(400);
        echo json_encode(array("success" => false, "error" => "Tous les champs sont requis"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $titre = strip_tags($data['titre']);
    $description = strip_tags($data['description']);
    $ingredients = strip_tags($data['ingredients']);
    $etapes_preparation = strip_tags($data['etapes_preparation']);
    $temps_preparation = strip_tags($data['temps_preparation']);
    $temps_cuisson = strip_tags($data['temps_cuisson']);
    $imageBase64 = $data['image'];
    $id_auteur = strip_tags($data['id_auteur']);

    // Vérifie si l'auteur existe dans la base de données
    $author = $databaseManager->getUserById($id_auteur);
    if (!$author) {
        http_response_code(404);
        echo json_encode(array("success" => false, "error" => "L'auteur specifie n'existe pas"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Vérifie s'il existe déjà une recette avec le même titre pour cet auteur
    $existing_recipe = $databaseManager->getRecipeByTitleAndAuthor($titre, $id_auteur);
    if ($existing_recipe) {
        http_response_code(409);
        echo json_encode(array("success" => false, "error" => "Une recette avec ce titre existe déjà pour cet utilisateur"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Vérifie si une image a été envoyée
    if (!empty($imageBase64)) {
        // Décode l'image encodée en base64
        $imageData = base64_decode($imageBase64);
        $imageName = "rcp" . md5(uniqid()) . '.jpg';
        $uploadPath = __DIR__ . '/images/' . $imageName;
        // Sauvegarde l'image sur le serveur
        if (!file_put_contents($uploadPath, $imageData)) {
            http_response_code(500);
            echo json_encode(array("success" => false, "error" => "Une erreur est survenue lors de l'enregistrement de l'image"), JSON_UNESCAPED_UNICODE);
            exit;
        }
    } else {
        // Utilise une image par défaut si aucune image n'est envoyée
        $imageName = 'img_defaut.jpg';
    }

    $id_recette = $databaseManager->addRecipe($titre, $description, $ingredients, $etapes_preparation, $temps_preparation, $temps_cuisson, $imageName, $id_auteur);

    if ($id_recette) {
        http_response_code(201);
        echo json_encode(array("success" => true, "id_recette" => $id_recette, "message" => "Recette ajoutée avec succès"), JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(500);
        echo json_encode(array("success" => false, "error" => "Impossible d'ajouter la recette"), JSON_UNESCAPED_UNICODE);
    }
} else {
    http_response_code(405);
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
