# 📖 Guide d'Intégration — API Biométrie

> **Destinataire :** Équipe d'intégration / Fournisseur d'équipement biométrique
> **Version :** 1.1
> **Date :** Septembre 2026

Ce document décrit comment envoyer des données biométriques depuis votre équipement vers le serveur d'enrôlement centralisé.

---

## 1. 🔗 Informations de Connexion

| Paramètre | Valeur |
| :--- | :--- |
| **Méthode HTTP** | `POST` |
| **URL** | `http://<ADRESSE_IP_SERVEUR>:8080/api/enroll` |
| **En-tête requis** | `Content-Type: application/json` |
| **Authentification** | Aucune (réseau local sécurisé) |

> ⚠️ **Important :** Remplacez `<ADRESSE_IP_SERVEUR>` par l'adresse IP réelle du serveur qui vous sera communiquée séparément.

---

## 2. 📝 Format de la Requête (JSON)

### 2.1 Structure complète

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
      "position": "left_0",
      "templateBase64": "Rk1SACAy...",
      "imageBase64": "Qk2GAAAA...",
      "deviceModel": "TR760",
      "qualite": 85,
      "tempsCaptureMs": 320
    },
    {
      "position": "right_0",
      "templateBase64": "Rk1SACAy...",
      "imageBase64": "Qk2GAAAA...",
      "deviceModel": "TR760",
      "qualite": 72,
      "tempsCaptureMs": 410
    }
  ],
  "iris": [
    {
      "position": "left",
      "templateBase64": "AAABAAAA..."
    },
    {
      "position": "right",
      "templateBase64": "AAABAAAA..."
    }
  ]
}
```

---

### 2.2 Description détaillée des champs

#### 👤 Données d'identité (racine de l'objet)

| Champ | Type | Requis | Description |
| :--- | :--- | :---: | :--- |
| `nom` | `String` | ✅ | Nom de famille de l'individu. |
| `prenom` | `String` | ✅ | Prénom de l'individu. |
| `sexe` | `String` | ✅ | Sexe : `"M"` pour Masculin, `"F"` pour Féminin. |
| `dateNaissance` | `String` | ✅ | Date de naissance au format `JJ/MM/AAAA`. Ex: `"15/03/1985"`. |
| `localite` | `String` | ✅ | Localité, ville ou région de l'individu. |
| `faceToken` | `String` | ⬜ | Identifiant unique (token) retourné par le module de reconnaissance faciale du SDK. Laisser vide si non disponible. |
| `facePhotoBase64` | `String` | ⬜ | Photo du visage encodée en **Base64**. Format attendu : **JPEG**. |

---

#### 🖐️ Empreintes digitales — Tableau `empreintes[]`

Chaque objet du tableau représente **une empreinte d'un doigt**.

| Champ | Type | Requis | Description |
| :--- | :--- | :---: | :--- |
| `position` | `String` | ✅ | Identifiant du doigt capturé (voir tableau de référence ci-dessous). |
| `templateBase64` | `String` | ✅ | Gabarit (template) ISO/IEC 19794-2 de l'empreinte encodé en **Base64**. |
| `imageBase64` | `String` | ⬜ | Image brute de l'empreinte au format **BMP**, encodée en **Base64**. |
| `deviceModel` | `String` | ✅ | **Nom ou référence du capteur** utilisé. Ex: `"TR760"`, `"HF4000"`, `"Futronic-FS88"`. Obligatoire pour les rapports comparatifs. |
| `qualite` | `Integer` | ✅ | **Score de qualité** de l'empreinte retourné par le SDK (score NFIQ de `0` à `100`). Un score **≥ 60** est considéré acceptable. |
| `tempsCaptureMs` | `Long` | ✅ | **Durée de la capture** en millisecondes, mesurée côté équipement. Indispensable pour les tests de performance. |

##### 📌 Référence des valeurs de `position` pour les doigts

| Valeur `position` | Doigt correspondant |
| :--- | :--- |
| `left_0` | Main Gauche — Auriculaire (5ème doigt) |
| `left_1` | Main Gauche — Annulaire |
| `left_2` | Main Gauche — Majeur |
| `left_3` | Main Gauche — Index |
| `thumb_0` | Main Gauche — Pouce |
| `right_0` | Main Droite — Auriculaire (5ème doigt) |
| `right_1` | Main Droite — Annulaire |
| `right_2` | Main Droite — Majeur |
| `right_3` | Main Droite — Index |
| `thumb_1` | Main Droite — Pouce |

> 💡 Vous pouvez envoyer uniquement les doigts disponibles. Il n'est pas obligatoire d'envoyer les 10 doigts.

---

#### 👁️ Iris — Tableau `iris[]`

Chaque objet représente **une capture d'iris** (œil gauche ou droit).

| Champ | Type | Requis | Description |
| :--- | :--- | :---: | :--- |
| `position` | `String` | ✅ | Œil capturé : `"left"` (gauche) ou `"right"` (droit). |
| `templateBase64` | `String` | ✅ | Gabarit de l'iris encodé en **Base64** (format ISO/IEC 19794-6 recommandé). |

---

## 3. ✅ Format de la Réponse

### En cas de succès — `200 OK`

```json
{
  "status": "success",
  "id": 42
}
```

| Champ | Description |
| :--- | :--- |
| `status` | Vaut toujours `"success"` en cas de succès. |
| `id` | L'identifiant unique de l'enrôlement créé en base de données. |

### En cas d'erreur — `500 Internal Server Error`

```json
{
  "status": "error",
  "message": "Description détaillée de l'erreur..."
}
```

---

## 4. 💻 Exemples d'Appel

### Avec `curl` (ligne de commande)

```bash
curl -X POST "http://192.168.1.100:8080/api/enroll" \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Dupont",
    "prenom": "Jean",
    "sexe": "M",
    "dateNaissance": "01/01/1990",
    "localite": "Paris",
    "empreintes": [
      {
        "position": "right_3",
        "templateBase64": "Rk1SACAy...",
        "deviceModel": "MON_EQUIPEMENT",
        "qualite": 80,
        "tempsCaptureMs": 350
      }
    ]
  }'
```

### Avec Java (OkHttp)

```java
OkHttpClient client = new OkHttpClient();

String json = "{ \"nom\": \"Dupont\", \"prenom\": \"Jean\", ... }";

RequestBody body = RequestBody.create(json, MediaType.get("application/json"));

Request request = new Request.Builder()
    .url("http://192.168.1.100:8080/api/enroll")
    .post(body)
    .build();

Response response = client.newCall(request).execute();
System.out.println(response.body().string());
```

### Avec Python (requests)

```python
import requests
import base64

# Encoder une image en Base64
with open("empreinte.bmp", "rb") as f:
    image_b64 = base64.b64encode(f.read()).decode("utf-8")

payload = {
    "nom": "Dupont",
    "prenom": "Jean",
    "sexe": "M",
    "dateNaissance": "01/01/1990",
    "localite": "Paris",
    "empreintes": [
        {
            "position": "right_3",
            "templateBase64": "Rk1SACAy...",
            "imageBase64": image_b64,
            "deviceModel": "MON_EQUIPEMENT",
            "qualite": 80,
            "tempsCaptureMs": 350
        }
    ]
}

response = requests.post("http://192.168.1.100:8080/api/enroll", json=payload)
print(response.json())
```

---

## 5. ℹ️ Notes Importantes

- **Encodage Base64 :** Tous les champs `*Base64` doivent être encodés en Base64 **standard** (RFC 4648), sans saut de ligne.
- **Format de date :** Le champ `dateNaissance` doit respecter strictement le format `JJ/MM/AAAA`.
- **Champs automatiques :** Les champs `id`, `date_creation`, `date_capture` et les clés étrangères sont gérés **automatiquement** par le serveur. Ne les envoyez pas.
- **Champs de benchmarking :** `deviceModel`, `qualite` et `tempsCaptureMs` sont **obligatoires** car ils permettent de générer les rapports comparatifs de performance entre équipements.
- **Nombre d'empreintes :** Vous pouvez envoyer de 1 à 10 empreintes par requête. Chaque doigt doit avoir sa propre entrée dans le tableau `empreintes[]`.

---

*Pour toute question technique, contactez l'équipe projet.*
