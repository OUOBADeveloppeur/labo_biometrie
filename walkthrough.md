# Intégration du Backend PostgreSQL & Spring Boot

## Ce qui a été accompli
Nous avons migré avec succès le stockage des données biométriques vers un serveur distant (API Spring Boot + PostgreSQL) tout en conservant la sauvegarde locale pour plus de robustesse.

### 1. API Spring Boot (`backend_bioapi`)
- **Projet créé** via Spring Initializr (Java 17, Spring Boot 3.2.x).
- **Entités JPA** : `Utilisateur`, `Empreinte`, `Iris` avec une relation `OneToMany`.
- **Contrôleur REST** : Endpoint `POST /api/enroll` permettant de recevoir les données sous format JSON (y compris les images et templates Base64).
- **Base de données** : L'API est configurée pour écrire dans la base par défaut `postgres` via Hibernate.
- **Tableau de Bord** : Une vue web créée avec Thymeleaf pour lister les personnes enregistrées.
  - Accessible via : [http://localhost:8080/](http://localhost:8080/)

### 2. Application Android (`MultimodalEnrollmentActivity`)
- **OkHttp** : Ajout de la dépendance dans `build.gradle` pour effectuer des appels réseau.
- **Envoi des données** : 
  - La méthode `saveAllData()` a été modifiée. 
  - Elle construit un objet JSON (contenant les informations utilisateur, les empreintes, et l'iris) puis l'envoie via une requête HTTP POST.
  - L'IP a été configurée statiquement vers `192.168.11.51:8080` (l'adresse de votre PC sur le réseau local actuel).
- **Compilation & Installation** : Le Gradle build a été corrigé (imports de `NonNull`, `JSONObject`, `OkHttp`) et installé sur la tablette HF-TR760.

## Validation des Tests
> [!IMPORTANT]
> - Effectuez une capture complète (visage, empreinte, iris).
> - Cliquez sur "**Save**" : un dialogue de chargement affichera "Envoi vers le serveur...".
> - Un message vert (Toast) indiquera "Sauvegarde locale et Serveur réussie !".
> - Allez sur [http://localhost:8080/](http://localhost:8080/) pour voir les résultats dans le tableau.
