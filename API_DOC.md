 # Documentation API Biométrie

Ce document décrit l'API REST permettant l'enrôlement biométrique d'un utilisateur.

## 🔗 Informations Générales

*   **Méthode HTTP** : `POST`
*   **URL de base** : `http://<ADRESSE_IP_SERVEUR>:8080/api/enroll`
*   **En-tête (Header) requis** : `Content-Type: application/json`

---

## 📝 Format de la Requête (JSON)

### Exemple de corps de la requête (Payload)

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "sexe": "M",
  "dateNaissance": "01/01/1990",
  "localite": "Paris",
  "faceToken": "token-facial-généré",
  "facePhotoBase64": "iVBORw0KGgoAAAANSUhEUgAA...", 
  "empreintes": [
    {
      "position": "left_0", 
      "templateBase64": "donnees_empreinte_en_base64...",
      "imageBase64": "donnees_image_bmp_en_base64..."
    },
    {
      "position": "right_1",
      "templateBase64": "donnees_empreinte_en_base64...",
      "imageBase64": "donnees_image_bmp_en_base64..."
    }
  ],
  "iris": [
    {
      "position": "left",
      "templateBase64": "donnees_iris_en_base64..."
    }
  ]
}
```

### Description des Champs

| Champ | Type | Description |
| :--- | :--- | :--- |
| `nom` | String | Nom de famille de l'utilisateur. |
| `prenom` | String | Prénom de l'utilisateur. |
| `sexe` | String | Sexe de l'utilisateur (ex: "M", "F"). |
| `dateNaissance` | String | Date de naissance de l'utilisateur. |
| `localite` | String | Localité (ou ville) de l'utilisateur. |
| `faceToken` | String | Jeton (Token) unique de reconnaissance faciale. |
| `facePhotoBase64` | String | (Optionnel) Photo du visage encodée en format Base64 (JPEG). |
| `empreintes` | Array | (Optionnel) Liste d'objets représentant les empreintes digitales. |
| ↳ `position` | String | Indique le doigt capturé (ex: `left_0`, `right_1`, `thumb_1`). |
| ↳ `templateBase64` | String | Le gabarit (template) de l'empreinte encodé en Base64. |
| ↳ `imageBase64` | String | (Optionnel) L'image de l'empreinte au format BMP encodée en Base64. |
| `iris` | Array | (Optionnel) Liste d'objets représentant les captures d'iris. |
| ↳ `position` | String | Indique l'œil capturé (ex: `left`, `right`). |
| ↳ `templateBase64` | String | Le gabarit de l'iris encodé en Base64. |

> **Note :** Techniquement au niveau de la base de données, aucun champ n'est strictement obligatoire. Toutefois, il est fortement recommandé d'envoyer à minima les informations d'identité (nom/prénom) ou un identifiant biométrique.

### 🗄️ Correspondance avec la Base de Données

Le document décrit les champs à **envoyer** depuis la tablette (le payload JSON). 
Certains champs que vous voyez dans votre base de données ne sont pas dans ce document car ils sont **générés automatiquement par le serveur** Spring Boot et n'ont pas à être envoyés :
* `id` : Identifiant unique auto-incrémenté.
* `date_creation` (Table utilisateurs) / `date_capture` (Table empreintes/iris) : Renseignés automatiquement à l'heure du serveur.
* `utilisateur_id` : Clé étrangère gérée automatiquement par Hibernate.

---

## ✅ Format de la Réponse

**En cas de succès :** Code HTTP `200 OK`
```json
{
  "status": "success", 
  "id": 12
}
```

**En cas d'erreur :** Code HTTP `500 Internal Server Error`
```json
{
  "status": "error",
  "message": "Description détaillée de l'erreur..."
}
```
