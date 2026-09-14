# API Biométrie

Guide d'intégration pour les appareils biométriques qui communiquent avec le serveur central d'enrôlement.

## 1. Connexion au serveur

Le serveur est actuellement accessible sur le réseau local à l'adresse :

```text
http://192.168.11.156:8080
```

> Cette adresse est configurée en IP statique sur le serveur. Les appareils du réseau local peuvent donc utiliser cette adresse de manière durable.

### Paramètres communs

| Paramètre | Valeur |
| :--- | :--- |
| Protocole | HTTP |
| Adresse actuelle | `192.168.11.156` |
| Port | `8080` |
| Authentification | Aucune, réseau local |
| Format des données | JSON |
| En-tête POST | `Content-Type: application/json` |

Toutes les routes sont préfixées par `/api/enroll`.

## 2. Routes disponibles

| Méthode | Route | Utilisation |
| :--- | :--- | :--- |
| `POST` | `/api/enroll` | Créer un nouvel enregistrement biométrique. |
| `GET` | `/api/enroll` | Récupérer tous les enregistrements. |
| `GET` | `/api/enroll/{id}` | Récupérer un seul enregistrement par son identifiant. |

### 2.1 Récupérer tous les enregistrements

```http
GET http://192.168.11.156:8080/api/enroll
```

Réponse `200 OK` : un tableau JSON. Si aucun enregistrement n'existe, le serveur retourne un tableau vide `[]`.

```json
[
  {
    "id": 42,
    "nom": "Dupont",
    "prenom": "Jean",
    "sexe": "M",
    "dateNaissance": "01/01/1990",
    "localite": "Paris",
    "faceToken": "abc123-token-facial",
    "facePhotoBase64": "/9j/4AAQSkZJRgABAQAA...",
    "dateCreation": "2026-09-11T15:00:00",
    "empreintes": [
      {
        "id": 10,
        "positionDoigt": "right_3",
        "templateBase64": "Rk1SACAy...",
        "imageBase64": "Qk2GAAAA...",
        "deviceModel": "TR760",
        "qualite": 85,
        "tempsCaptureMs": 320,
        "dateCapture": "2026-09-11T15:00:01"
      }
    ],
    "iris": [
      {
        "id": 5,
        "positionOeil": "left",
        "templateBase64": "AAABAAAA...",
        "dateCapture": "2026-09-11T15:00:02"
      }
    ]
  }
]
```

Commande de test :

```bash
curl "http://192.168.11.156:8080/api/enroll"
```

### 2.2 Récupérer un enregistrement

Remplacez `42` par l'identifiant retourné après un POST.

```http
GET http://192.168.11.156:8080/api/enroll/42
```

La réponse `200 OK` contient un objet utilisateur avec ses empreintes et ses iris. Si l'identifiant n'existe pas, le serveur retourne `404 Not Found`.

```bash
curl "http://192.168.11.156:8080/api/enroll/42"
```

## 3. Créer un enregistrement

### Route

```http
POST http://192.168.11.156:8080/api/enroll
Content-Type: application/json
```

### Corps JSON

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "sexe": "M",
  "dateNaissance": "01/01/1990",
  "localite": "Paris",
  "faceToken": "abc123-token-facial",
  "facePhotoBase64": "/9j/4AAQSkZJRgABAQAA...",
  "empreintes": [
    {
      "position": "right_3",
      "templateBase64": "Rk1SACAy...",
      "imageBase64": "Qk2GAAAA...",
      "deviceModel": "TR760",
      "qualite": 85,
      "tempsCaptureMs": 320
    }
  ],
  "iris": [
    {
      "position": "left",
      "templateBase64": "AAABAAAA..."
    }
  ]
}
```

### Champs de la requête

#### Identité et visage

| Champ | Type | Description |
| :--- | :--- | :--- |
| `nom` | `String` | Nom de famille. |
| `prenom` | `String` | Prénom. |
| `sexe` | `String` | Sexe, par exemple `M` ou `F`. |
| `dateNaissance` | `String` | Date au format `JJ/MM/AAAA`. |
| `localite` | `String` | Ville ou région. |
| `faceToken` | `String` | Identifiant facial fourni par le SDK. |
| `facePhotoBase64` | `String` | Photo du visage en Base64, idéalement JPEG. |

#### `empreintes[]`

| Champ | Type | Description |
| :--- | :--- | :--- |
| `position` | `String` | Position du doigt, par exemple `right_3`. |
| `templateBase64` | `String` | Gabarit de l'empreinte en Base64. |
| `imageBase64` | `String` | Image de l'empreinte en Base64, si disponible. |
| `deviceModel` | `String` | Modèle du capteur, par exemple `TR760`. |
| `qualite` | `Integer` | Score de qualité fourni par le SDK. |
| `tempsCaptureMs` | `Long` | Durée de capture en millisecondes. |

Positions usuelles : `left_0`, `left_1`, `left_2`, `left_3`, `thumb_0`, `right_0`, `right_1`, `right_2`, `right_3`, `thumb_1`.

#### `iris[]`

| Champ | Type | Description |
| :--- | :--- | :--- |
| `position` | `String` | `left` pour l'oeil gauche ou `right` pour l'oeil droit. |
| `templateBase64` | `String` | Gabarit de l'iris en Base64. |

Les champs qui se terminent par `Base64` doivent contenir du Base64 standard, sans saut de ligne.

### Réponse en cas de succès

Code HTTP `200 OK` :

```json
{
  "status": "success",
  "id": 42
}
```

`id` est l'identifiant à utiliser ensuite avec `GET /api/enroll/{id}`.

### Réponse en cas de doublon

Code HTTP `409 Conflict` lorsqu'un token facial ou une identité existe déjà :

```json
{
  "status": "doublon",
  "raison": "Token facial déjà enregistré",
  "similarite": "100%",
  "message": "Cette personne est déjà enregistrée dans le système.",
  "existant": {
    "id": 42,
    "nom": "Dupont",
    "prenom": "Jean",
    "nbEmpreintes": 1,
    "nbIris": 1
  }
}
```

### Réponse en cas d'erreur serveur

Code HTTP `500 Internal Server Error` :

```json
{
  "status": "error",
  "message": "Description de l'erreur"
}
```

## 4. Exemples complets

### Envoi depuis un terminal

```bash
curl -X POST "http://192.168.11.156:8080/api/enroll" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "sexe": "M",
    "dateNaissance": "01/01/1990",
    "localite": "Paris",
    "empreintes": [],
    "iris": []
  }'
```

### Lecture depuis Python

```python
import requests

base_url = "http://192.168.11.156:8080/api/enroll"

# Tous les enregistrements
tous = requests.get(base_url, timeout=30)
tous.raise_for_status()
print(tous.json())

# Un enregistrement
enregistrement = requests.get(f"{base_url}/42", timeout=30)
if enregistrement.status_code == 200:
    print(enregistrement.json())
elif enregistrement.status_code == 404:
    print("Enregistrement introuvable")
```

## 5. Notes d'intégration

- Le serveur doit rester démarré et accessible sur le même réseau local que les appareils.
- Le port `8080` doit être autorisé par le pare-feu du serveur.
- Les champs `id`, `dateCreation`, `dateCapture` et les relations sont créés automatiquement par le serveur. Ils ne doivent pas être envoyés dans le POST.
- Après un POST réussi, conservez l'`id` retourné pour récupérer l'enregistrement avec le GET individuel.
- L'adresse `192.168.11.156` est configurée en IP statique sur l'interface Wi-Fi `wlo1`.
