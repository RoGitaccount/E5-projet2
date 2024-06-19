<?php

class DatabaseManager {
    private $connection;

    /**
     * Permet la connexion à la BDD 
     */
    public function __construct() {
        $config = include('Database.php');
        try {
            $this->connection = new PDO("mysql:host={$config['host']};dbname={$config['database']}", $config['username'], $config['password']);
            $this->connection->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        } catch (PDOException $e) {
            throw new Exception("Erreur de connexion à la base de données : " . $e->getMessage());
        }
    }

    /**
     * Permet la connexion à l'utilisateur
     *
     * @param string $email
     * @param string $password
     * @return array|false
     */
    public function connectUser($email, $password) {
        $query = $this->connection->prepare("SELECT * FROM utilisateurs WHERE email = :email");
        $query->bindValue(":email", $email, PDO::PARAM_STR);
        $query->execute();

        if ($query->rowCount() > 0) {
            $user = $query->fetch(PDO::FETCH_ASSOC);

            $hashedPassword = $user['mot_de_passe']; // Récupérer le mot de passe hashé stocké dans la base de données

            // Vérifie si le mot de passe fourni correspond au mot de passe haché stocké dans la base de données
            if (password_verify($password, $hashedPassword)) {
                // Si la vérification réussit, retourne un tableau associatif contenant les informations de l'utilisateur
                return $user;
            } else {
                // Si la vérification échoue, retourne false indiquant que le mot de passe est incorrect
                return false;
            }
        } else {
            // Si aucun utilisateur n'est trouvé avec l'adresse e-mail fournie, retourne false indiquant que l'e-mail est incorrect
            return false;
        }
    }

    /**
     * Permet la création d'un nouveau compte 
     *
     * @param string $nom
     * @param string $prenom
     * @param string $email
     * @param string $password
     * @param string $role
     * @return void
     */
    public function createUser($nom, $prenom, $email, $password, $role = "utilisateur") {
        // Hashage du mot de passe
        $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

        // Vérification si l'email existe déjà dans la base de données
        $sql = "SELECT * FROM utilisateurs WHERE email = :email";
        $query = $this->connection->prepare($sql);
        $query->bindValue(":email", $email, PDO::PARAM_STR);
        $query->execute();

        if ($query->rowCount() > 0) {
            // L'email existe déjà dans la base de données
            return false;
        } else {
            // L'email est unique, procéder à l'insertion
            $sql = "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role) VALUES (:nom, :prenom, :email, :mdp, :role)";
            $query = $this->connection->prepare($sql);
            $query->bindValue(":nom", $nom, PDO::PARAM_STR);
            $query->bindValue(":prenom", $prenom, PDO::PARAM_STR);
            $query->bindValue(":email", $email, PDO::PARAM_STR);
            $query->bindValue(":mdp", $hashedPassword, PDO::PARAM_STR);
            $query->bindValue(":role", $role, PDO::PARAM_STR);
            $result = $query->execute();

            if ($result) {
                // L'insertion a réussi, retourner l'ID de l'utilisateur
                return $this->connection->lastInsertId();
            } else {
                // L'insertion a échoué
                return false;
            }
        }
    }


    public function showAllRecettes($id_utilisateur) {
        try {
            // Préparer la requête SQL
            $sql = "SELECT 
                        recettes.*, 
                        utilisateurs.prenom,
                        CASE WHEN favoris.id_recette IS NOT NULL THEN true ELSE false END AS isFavorite
                    FROM 
                        recettes
                    JOIN 
                        utilisateurs ON recettes.id_auteur = utilisateurs.id_utilisateur
                    LEFT JOIN 
                        favoris ON recettes.id_recette = favoris.id_recette AND favoris.id_utilisateur = :id_utilisateur
                    ORDER BY 
                        recettes.date_creation DESC";
    
            // Préparer la requête PDO
            $stmt = $this->connection->prepare($sql);
    
            // Lier les paramètres de la requête
            $stmt->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
    
            // Exécuter la requête
            $stmt->execute();
    
            // Récupérer les résultats
            $recettes = $stmt->fetchAll(PDO::FETCH_ASSOC);
    
            return $recettes;
        } catch (PDOException $e) {
            // Gérer les erreurs de requête PDO
            echo "Erreur lors de l'exécution de la requête : " . $e->getMessage();
            return false;
        }
    }
    
    /**
     * Vérifie si une recette est dans les favoris de l'utilisateur
     *
     * @param int $id_recette
     * @param int $id_utilisateur
     * @return bool
     */
    public function recetteDansFavoris($id_recette, $id_utilisateur) {
        $sql = "SELECT COUNT(*) FROM Favoris WHERE id_utilisateur = :id_utilisateur AND id_recette = :id_recette";
        $stmt = $this->connection->prepare($sql);
        $stmt->execute(["id_utilisateur" => $id_utilisateur, "id_recette" => $id_recette]);
        return (bool) $stmt->fetchColumn();
    }

    /**
     * Ajoute une recette aux favoris de l'utilisateur
     *
     * @param int $id_recette
     * @param int $id_utilisateur
     * @return bool
     */
    public function ajouterRecetteAuxFavoris($id_recette, $id_utilisateur) {
        $sql = "INSERT INTO Favoris (id_utilisateur, id_recette) VALUES (:id_utilisateur, :id_recette)";
        $stmt = $this->connection->prepare($sql);
        return $stmt->execute(["id_utilisateur" => $id_utilisateur, "id_recette" => $id_recette]);
    }

    /**
     * Retire une recette des favoris de l'utilisateur
     *
     * @param int $id_recette
     * @param int $id_utilisateur
     * @return bool
     */
    public function retirerRecetteDesFavoris($id_recette, $id_utilisateur) {
        $sql = "DELETE FROM Favoris WHERE id_utilisateur = :id_utilisateur AND id_recette = :id_recette";
        $stmt = $this->connection->prepare($sql);
        return $stmt->execute(["id_utilisateur" => $id_utilisateur, "id_recette" => $id_recette]);
    }

        /**
     * Récupère une seule recette en fonction de son ID
     *
     * @param int $id_recette L'ID de la recette à récupérer
     * @return array|false Les détails de la recette si trouvée, false sinon
     */
    public function showOneRecette($id_recette) {
        $sql = "SELECT recettes.*, utilisateurs.prenom 
                FROM utilisateurs 
                JOIN recettes ON utilisateurs.id_utilisateur = recettes.id_auteur
                WHERE recettes.id_recette = :id_recette";
        $query = $this->connection->prepare($sql);
        $query->bindValue(":id_recette", $id_recette, PDO::PARAM_INT);
        $query->execute();
        return $query->fetch(PDO::FETCH_ASSOC);
    }

/**
 * Vérifie si une recette existe dans la base de données
 *
 * @param int $id_recette L'ID de la recette à vérifier
 * @return bool True si la recette existe, false sinon
 */
public function recetteExiste($id_recette) {
    $sql = "SELECT COUNT(*) AS count FROM recettes WHERE id_recette = :id_recette";
    $query = $this->connection->prepare($sql);
    $query->bindParam(":id_recette", $id_recette, PDO::PARAM_INT);
    $query->execute();
    $result = $query->fetch(PDO::FETCH_ASSOC);
    return ($result && isset($result['count']) && $result['count'] > 0);
}



    /**
     * Ajoute une nouvelle recette à la base de données
     *
     * @param string $titre Titre de la recette
     * @param string $description Description de la recette
     * @param string $ingredients Liste des ingrédients de la recette
     * @param string $etapes_preparation Étapes de préparation de la recette
     * @param string $temps_preparation Temps de préparation de la recette
     * @param string $temps_cuisson Temps de cuisson de la recette
     * @param string $image Nom de l'image de la recette
     * @param int $id_auteur ID de l'auteur de la recette
     * @return int|false L'ID de la recette ajoutée, ou false en cas d'échec
    */
    public function addRecipe($titre, $description, $ingredients, $etapes_preparation, $temps_preparation, $temps_cuisson, $image, $id_auteur) {
        $sql = "INSERT INTO recettes (titre, description, ingredients, etapes_preparation, temps_preparation, temps_cuisson, image, id_auteur, date_creation) 
                VALUES (:titre, :description, :ingredients, :etapes_preparation, :temps_preparation, :temps_cuisson, :image, :id_auteur, NOW())";
        $query = $this->connection->prepare($sql);
        $query->bindValue(':titre', $titre, PDO::PARAM_STR);
        $query->bindValue(':description', $description, PDO::PARAM_STR);
        $query->bindValue(':ingredients', $ingredients, PDO::PARAM_STR);
        $query->bindValue(':etapes_preparation', $etapes_preparation, PDO::PARAM_STR);
        $query->bindValue(':temps_preparation', $temps_preparation, PDO::PARAM_STR);
        $query->bindValue(':temps_cuisson', $temps_cuisson, PDO::PARAM_STR);
        $query->bindValue(':image', $image, PDO::PARAM_STR);
        $query->bindValue(':id_auteur', $id_auteur, PDO::PARAM_INT);
        $result = $query->execute();
    
        if ($result) {
            // Récupérer l'ID de la recette nouvellement insérée
            $id_recette = $this->connection->lastInsertId();
            return $id_recette;
        } else {
            return false;
        }
    }
    
        /**
     * Récupère une recette par son titre et l'ID de l'auteur
     *
     * @param string $titre Le titre de la recette à rechercher
     * @param int $id_auteur L'ID de l'auteur de la recette
     * @return array|false Les détails de la recette si trouvée, false sinon
     */
    public function getRecipeByTitleAndAuthor($titre, $id_auteur) {
        $sql = "SELECT * FROM recettes WHERE titre = :titre AND id_auteur = :id_auteur";
        $query = $this->connection->prepare($sql);
        $query->bindValue(":titre", $titre, PDO::PARAM_STR);
        $query->bindValue(":id_auteur", $id_auteur, PDO::PARAM_INT);
        $query->execute();
        return $query->fetch(PDO::FETCH_ASSOC);
    }

        /**
     * Récupère un utilisateur par son ID
     *
     * @param int $id_utilisateur L'ID de l'utilisateur à récupérer
     * @return array|false Les détails de l'utilisateur si trouvé, false sinon
     */
    public function getUserById($id_utilisateur) {
        $sql = "SELECT * FROM utilisateurs WHERE id_utilisateur = :id_utilisateur";
        $query = $this->connection->prepare($sql);
        $query->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
        $query->execute();
        return $query->fetch(PDO::FETCH_ASSOC);
    }


    public function deleteRecipe($id_recette) {
        try {
            // Commencer une transaction
            $this->connection->beginTransaction();
    
            // Vérifier d'abord si la recette existe
            $recipeExists = $this->showOneRecette($id_recette);
            
            if (!$recipeExists) {
                // Aucune recette trouvée avec cet ID, retourner false
                return false;
            }
    
            // Récupérer le nom du fichier image associé à la recette
            $sqlGetImage = "SELECT image FROM recettes WHERE id_recette = :id_recette";
            $queryGetImage = $this->connection->prepare($sqlGetImage);
            $queryGetImage->bindValue(":id_recette", $id_recette, PDO::PARAM_INT);
            $queryGetImage->execute();
            $imageData = $queryGetImage->fetch(PDO::FETCH_ASSOC);
    
            // Si une image est associée à la recette et si ce n'est pas l'image par défaut, supprimer le fichier du dossier "images"
            if ($imageData && $imageData['image'] !== 'img_defaut.jpg') {
                // Vérifier si le nom de l'image est différent de l'image par défaut
                $defaultImage = 'img_defaut.jpg';
                if ($imageData['image'] !== $defaultImage) {
                    $filePath = "images/" . $imageData['image'];
                    // Vérifier si le fichier existe avant de le supprimer
                    if (file_exists($filePath)) {
                        unlink($filePath);
                    }
                }
            }
    
            // Supprimer les favoris associés à la recette
            $sqlDeleteFavorites = "DELETE FROM favoris WHERE id_recette = :id_recette";
            $queryDeleteFavorites = $this->connection->prepare($sqlDeleteFavorites);
            $queryDeleteFavorites->bindValue(":id_recette", $id_recette, PDO::PARAM_INT);
            $queryDeleteFavorites->execute();
    
            // Supprimer la recette de la base de données
            $sqlDeleteRecipe = "DELETE FROM recettes WHERE id_recette = :id_recette";
            $queryDeleteRecipe = $this->connection->prepare($sqlDeleteRecipe);
            $queryDeleteRecipe->bindValue(":id_recette", $id_recette, PDO::PARAM_INT);
            $queryDeleteRecipe->execute();
    
            // Valider la transaction
            $this->connection->commit();
            return true;
        } catch (Exception $e) {
            // En cas d'erreur, annuler la transaction et retourner false
            $this->connection->rollBack();
            return false;
        }
    }

    public function showfavRecipe($id_utilisateur) {
        $sql_favoris = "SELECT 
                            recettes.*, 
                            utilisateurs.prenom
                        FROM 
                            recettes
                        JOIN 
                            favoris ON recettes.id_recette = favoris.id_recette
                        JOIN 
                            utilisateurs ON recettes.id_auteur = utilisateurs.id_utilisateur
                        WHERE 
                            favoris.id_utilisateur = :id_utilisateur
                        ORDER BY 
                            recettes.date_creation DESC";
    
        $stmt_favoris = $this->connection->prepare($sql_favoris);
        $stmt_favoris->execute(["id_utilisateur" => $id_utilisateur]);
        return $stmt_favoris->fetchAll(PDO::FETCH_ASSOC);
    }

    public function showAllUsers() {
        $sql = "SELECT * FROM utilisateurs WHERE id_utilisateur NOT IN (8) ORDER BY id_utilisateur DESC";
        $query = $this->connection->query($sql);
        return $query->fetchAll(PDO::FETCH_ASSOC);
    }
    

    /**
     * Met à jour les informations d'un utilisateur
     *
     * @param int $id_utilisateur L'ID de l'utilisateur à mettre à jour
     * @param string $nom Le nouveau nom de l'utilisateur
     * @param string $prenom Le nouveau prénom de l'utilisateur
     * @param string $email Le nouvel email de l'utilisateur
     * @param string $role Le nouveau rôle de l'utilisateur
     * @return bool True si la mise à jour a réussi, False sinon
     */
    public function updateUser($id_utilisateur, $nom, $prenom, $email, $role) {
        // Vérifier si l'email existe déjà
        if ($this->emailExists($email, $id_utilisateur)) {
            return false; // L'email existe déjà
        }

        // Préparer la requête de mise à jour
        $sql = "UPDATE utilisateurs 
                SET nom = :nom, prenom = :prenom, email = :email, Role = :role 
                WHERE id_utilisateur = :id_utilisateur";
        $query = $this->connection->prepare($sql);

        // Lier les paramètres de la requête
        $query->bindValue(':id_utilisateur', $id_utilisateur, PDO::PARAM_INT);
        $query->bindValue(':nom', $nom, PDO::PARAM_STR);
        $query->bindValue(':prenom', $prenom, PDO::PARAM_STR);
        $query->bindValue(':email', $email, PDO::PARAM_STR);
        $query->bindValue(':role', $role, PDO::PARAM_STR);

        // Exécuter la requête et retourner le résultat
        return $query->execute();
    }

    // Méthode pour vérifier si un email existe déjà dans la base de données
    public function emailExists($email, $id_utilisateur = null) {
        $sql = "SELECT COUNT(*) FROM utilisateurs WHERE email = :email";
        if ($id_utilisateur !== null) {
            $sql .= " AND id_utilisateur != :id_utilisateur";
        }
        $query = $this->connection->prepare($sql);
        $query->bindValue(':email', $email, PDO::PARAM_STR);
        if ($id_utilisateur !== null) {
            $query->bindValue(':id_utilisateur', $id_utilisateur, PDO::PARAM_INT);
        }
        $query->execute();
        return $query->fetchColumn() > 0;
    }

    

    public function updateRecipe($id_recette, $titre, $description, $ingredients, $etapes_preparation, $temps_preparation, $temps_cuisson, $image = null) {
        // Construire la requête SQL de mise à jour
        $sql = "UPDATE recettes 
                SET titre = :titre, 
                    description = :description, 
                    ingredients = :ingredients, 
                    etapes_preparation = :etapes_preparation, 
                    temps_preparation = :temps_preparation, 
                    temps_cuisson = :temps_cuisson";
    
        // Ajouter le champ image si une nouvelle image est fournie
        if ($image !== null) {
            $sql .= ", image = :image";
        }
    
        $sql .= " WHERE id_recette = :id_recette";
    
        // Préparer la requête
        $query = $this->connection->prepare($sql);
    
        // Lier les valeurs aux paramètres
        $query->bindValue(':titre', $titre, PDO::PARAM_STR);
        $query->bindValue(':description', $description, PDO::PARAM_STR);
        $query->bindValue(':ingredients', $ingredients, PDO::PARAM_STR);
        $query->bindValue(':etapes_preparation', $etapes_preparation, PDO::PARAM_STR);
        $query->bindValue(':temps_preparation', $temps_preparation, PDO::PARAM_STR);
        $query->bindValue(':temps_cuisson', $temps_cuisson, PDO::PARAM_STR);
        if ($image !== null) {
            $query->bindValue(':image', $image, PDO::PARAM_STR);
        }
        $query->bindValue(':id_recette', $id_recette, PDO::PARAM_INT);
    
        // Exécuter la requête
        return $query->execute();
    }
    


    public function showOneUser($id_utilisateur) {
        $sql = "SELECT * FROM utilisateurs WHERE id_utilisateur = :id_utilisateur";
        $query = $this->connection->prepare($sql);
        $query->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
        $query->execute();
        return $query->fetch(PDO::FETCH_ASSOC);
    }


    
    public function deleteUser($id_utilisateur) {
        try {
            // Commencer une transaction
            $this->connection->beginTransaction();
    
            // Supprimer d'abord les favoris associés à l'utilisateur
            $sqlDeleteFavorites = "DELETE FROM favoris WHERE id_utilisateur = :id_utilisateur";
            $queryDeleteFavorites = $this->connection->prepare($sqlDeleteFavorites);
            $queryDeleteFavorites->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
            $queryDeleteFavorites->execute();
    
            // Supprimer les recettes associées à l'utilisateur
            $sqlDeleteRecipes = "DELETE FROM recettes WHERE id_auteur = :id_utilisateur";
            $queryDeleteRecipes = $this->connection->prepare($sqlDeleteRecipes);
            $queryDeleteRecipes->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
            $queryDeleteRecipes->execute();
    
            // Supprimer l'utilisateur de la base de données
            $sqlDeleteUser = "DELETE FROM utilisateurs WHERE id_utilisateur = :id_utilisateur";
            $queryDeleteUser = $this->connection->prepare($sqlDeleteUser);
            $queryDeleteUser->bindValue(":id_utilisateur", $id_utilisateur, PDO::PARAM_INT);
            $queryDeleteUser->execute();
    
            // Valider la transaction
            $this->connection->commit();
            return true;
        } catch (Exception $e) {
            // En cas d'erreur, annuler la transaction et retourner false
            $this->connection->rollBack();
            return false;
        }
    }
    

}
?>