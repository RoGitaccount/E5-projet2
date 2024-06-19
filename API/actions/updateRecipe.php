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
    if (!isset($data['id_recette'])) {
        http_response_code(400);
        echo json_encode(array("success" => false, "error" => "L'ID de la recette est requis"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    $id_recette = strip_tags($data['id_recette']);

    // Récupérer la recette existante
    $existing_recipe = $databaseManager->showOneRecette($id_recette);
    if (!$existing_recipe) {
        http_response_code(404);
        echo json_encode(array("success" => false, "error" => "La recette spécifiée n'existe pas"), JSON_UNESCAPED_UNICODE);
        exit;
    }

    // Utiliser les nouvelles valeurs ou les valeurs existantes si les nouvelles ne sont pas fournies ou sont vides ou contiennent seulement des espaces
    $titre = (!empty(trim($data['titre']))) ? strip_tags($data['titre']) : $existing_recipe['titre'];
    $description = (!empty(trim($data['description']))) ? strip_tags($data['description']) : $existing_recipe['description'];
    $ingredients = (!empty(trim($data['ingredients']))) ? strip_tags($data['ingredients']) : $existing_recipe['ingredients'];
    $etapes_preparation = (!empty(trim($data['etapes_preparation']))) ? strip_tags($data['etapes_preparation']) : $existing_recipe['etapes_preparation'];
    $temps_preparation = (!empty(trim($data['temps_preparation']))) ? strip_tags($data['temps_preparation']) : $existing_recipe['temps_preparation'];
    $temps_cuisson = (!empty(trim($data['temps_cuisson']))) ? strip_tags($data['temps_cuisson']) : $existing_recipe['temps_cuisson'];
    $imageBase64 = $data['image'] ?? null;

    $imageName = $existing_recipe['image'];
    if ($imageBase64 === null || trim($imageBase64) === '') {
        $imageName = $existing_recipe['image'];
    } else {
        $imageData = base64_decode($imageBase64);
        $imageName = "rcp" . md5(uniqid()) . '.jpg';
        $uploadPath = __DIR__ . '/images/' . $imageName;
        if (!file_put_contents($uploadPath, $imageData)) {
            http_response_code(500);
            echo json_encode(array("success" => false, "error" => "Une erreur est survenue lors de l'enregistrement de l'image"), JSON_UNESCAPED_UNICODE);
            exit;
        }
        // Supprimer l'ancienne image si elle n'est pas l'image par défaut
        if ($existing_recipe['image'] !== 'img_defaut.jpg') {
            unlink(__DIR__ . '/images/' . $existing_recipe['image']);
        }
    }

    $result = $databaseManager->updateRecipe($id_recette, $titre, $description, $ingredients, $etapes_preparation, $temps_preparation, $temps_cuisson, $imageName);

    if ($result) {
        http_response_code(200);
        echo json_encode(array("success" => true, "message" => "Recette mise à jour avec succès"), JSON_UNESCAPED_UNICODE);
    } else {
        http_response_code(500);
        echo json_encode(array("success" => false, "error" => "Impossible de mettre à jour la recette"), JSON_UNESCAPED_UNICODE);
    }
} else {
    http_response_code(405);
    echo json_encode(array("success" => false, "error" => "Méthode non autorisée"), JSON_UNESCAPED_UNICODE);
}
?>
