# RAPPORT TECHNIQUE : Évaluation et Comparaison des Équipements Biométriques

**Date :** 17 Septembre 2026  
**Destinataire :** Autorité Compétente / Comité de Pilotage  
**Objet :** Méthodologie d'évaluation et critères de sélection pour les équipements de capture biométrique.  

---

## 1. Contexte et Objectif

Dans le cadre du déploiement du nouveau système centralisé d'enrôlement biométrique multimodal (Visage, Empreintes, Iris), il est impératif de garantir l'acquisition de données de très haute qualité. La fiabilité du système d'identification national (ou institutionnel) repose directement sur la précision des capteurs utilisés sur le terrain.

L'objectif de ce rapport est de présenter la **méthodologie objective et automatisée** mise en place pour évaluer et comparer plusieurs modèles d'équipements biométriques (ex: tablettes HF-TR760, capteurs HF4000, etc.) en conditions réelles. Cette approche permettra à l'Autorité de prendre une décision d'acquisition éclairée, basée sur des données chiffrées plutôt que sur les seules déclarations des constructeurs.

---

## 2. Méthodologie de Collecte et de Benchmarking

Pour comparer les différents équipements, nous avons développé une **API de centralisation des enrôlements**. 

À chaque fois qu'un opérateur réalise un enrôlement sur le terrain, l'équipement transmet non seulement les données biométriques du citoyen, mais également un ensemble complet de **métadonnées techniques et de performance**. Le serveur central agrège ces informations, permettant de générer des tableaux de bord statistiques pour comparer les performances de chaque modèle d'équipement.

### Données d'identification de l'équipement remontées systématiquement :
- **Modèle du capteur (`deviceModel`)** : Permet d'isoler les statistiques par type de matériel.
- **Version du Firmware/SDK (`firmwareVersion`)** : Permet de vérifier si une mise à jour logicielle améliore les performances d'un équipement.
- **Résolution (`resolution`)** : Vérification du respect de la norme (généralement 500 DPI).

---

## 3. Critères d'Évaluation (KPIs de Performance)

Pour départager les équipements, le système évalue automatiquement chaque capture selon deux axes majeurs : la **Qualité Biométrique** et l'**Efficacité Opérationnelle**.

### A. Axe 1 : Qualité Biométrique (Précision)

Un bon équipement doit fournir des images nettes permettant une comparaison (matching) sans faille.

1. **Score NFIQ (NIST Fingerprint Image Quality)** :
   - *Standard international (ISO/IEC 29794-4).*
   - **Échelle :** de 1 (Excellent) à 5 (Inexploitable).
   - **Objectif d'évaluation :** Quel équipement produit le plus grand pourcentage d'empreintes classées NFIQ 1 ou 2 ?
2. **Score de Qualité SDK** :
   - Évaluation interne par l'algorithme du constructeur (sur 100).
   - **Objectif d'évaluation :** Vérifier la constance du capteur à fournir des images avec un contraste et une netteté optimaux (Score ≥ 60).
3. **Surface de Contact (`surfaceContact`)** :
   - Mesure la taille de l'empreinte capturée en pixels.
   - **Objectif d'évaluation :** Un capteur de mauvaise conception ergonomique entraînera de faibles surfaces de contact (l'usager pose mal son doigt). Nous mesurons quel capteur favorise la plus grande surface de capture.

### B. Axe 2 : Efficacité Opérationnelle (Vitesse et Ergonomie)

Un bon équipement doit permettre d'enrôler rapidement les citoyens pour éviter les files d'attente.

1. **Temps de Capture (`tempsCaptureMs`)** :
   - Mesure du délai (en millisecondes) entre le moment où le doigt est posé et le moment où l'image de bonne qualité est numérisée.
   - **Objectif d'évaluation :** Identifier l'équipement doté du processeur de traitement d'image le plus rapide.
2. **Nombre de Tentatives (`nombreTentatives`)** :
   - Le nombre de fois que l'opérateur doit relancer la capture pour obtenir une image valide.
   - **Objectif d'évaluation :** Un nombre élevé de tentatives indique un capteur capricieux, sensible à la lumière ou difficile à utiliser.
3. **Robustesse aux conditions extrêmes (`conditionCapture`)** :
   - Le système trace si les doigts sont "Secs", "Humides" ou "Normaux".
   - **Objectif d'évaluation :** Déterminer quel capteur s'en sort le mieux (maintient de bons scores NFIQ) face à des doigts abîmés ou des conditions d'humidité difficiles.

---

## 4. Résultats Attendus et Outil d'Aide à la Décision

Grâce à cette collecte structurée, la plateforme serveur sera en mesure de générer un rapport comparatif automatisé croisant ces critères. 

**Exemple de restitution qui sera présentée à l'Autorité :**
* "Sur 10 000 captures, l'équipement A obtient 85% de NFIQ 1 en un temps moyen de 320ms avec 1.2 tentatives."
* "L'équipement B obtient 60% de NFIQ 1 en un temps moyen de 800ms avec 2.5 tentatives, et échoue souvent sur les doigts secs."

### Conclusion

Cette méthodologie garantit une totale transparence et impartialité dans le processus de sélection du matériel. L'Autorité disposera de **preuves chiffrées, issues du terrain**, pour justifier ses choix d'investissements et s'assurer que l'équipement retenu garantira la pérennité et la sécurité de la base de données biométrique nationale.

---
*Document généré par l'équipe d'intégration logicielle.*
