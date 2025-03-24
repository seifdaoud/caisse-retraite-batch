# Caisse Retraite Batch

## Introduction

Ce projet est un exemple de **batch Spring Boot / Spring Batch** permettant de traiter un fichier CSV contenant des données de caisse de retraite (NSS, nom, prénom, etc.) et de les exporter dans un fichier Excel. Deux modes d’écriture sont proposés :

- **Multi-onglets** : Un fichier Excel unique comportant plusieurs feuilles générées dès que le nombre de lignes dépasse un seuil défini.
- **Multi-fichiers** : Plusieurs fichiers Excel (`output_1.xlsx`, `output_2.xlsx`, …) sont créés lorsque le seuil de lignes par fichier est atteint.

> **NB** : Le projet est facilement extensible pour gérer un très grand nombre de colonnes, des validations complexes et un monitoring complet de Spring Batch.

---

## Table des matières

1. [Fonctionnalités](#fonctionnalités)
2. [Prérequis](#prérequis)
3. [Installation & Compilation](#installation--compilation)
4. [Configuration](#configuration)
5. [Exécution](#exécution)
6. [Logs & Monitoring](#logs--monitoring)
7. [Structure du projet](#structure-du-projet)
8. [Classes principales](#classes-principales)
9. [Approfondissements techniques](#approfondissements-techniques)
10. [Personnalisation](#personnalisation)
11. [Contributeurs](#contributeurs)
12. [Licence](#licence)

---

## Fonctionnalités

- **Lecture de CSV** : Import des données depuis un fichier CSV.
- **Validation JSR‑303** : Utilisation d’annotations (ex. `@Min`, `@NotBlank`, etc.) via un `BeanValidatingItemProcessor`.
- **Skip Policy** : Gestion d’un seuil maximum d’erreurs invalides (par exemple 50) avant de stopper le job.
- **Écriture SXSSF (streaming) dans Excel** :
  - **Multi-onglets** : Un seul fichier avec plusieurs sheets.
  - **Multi-fichiers** (optionnel) : Plusieurs fichiers générés en fonction du nombre de lignes.
- **Multi-threading** (optionnel) : Parallélisation de la lecture et/ou de la transformation.
- **Reporting** : Utilisation des logs Spring Boot / Spring Batch et stockage de l’historique dans les tables `BATCH_*`.
- **Configuration flexible** : Paramétrage via `application.properties`.

---

## Prérequis

- **Java 8** (ou version ultérieure)
- **Maven 3** (ou supérieur) pour compiler et exécuter le projet.
- (Optionnel) **Base de données** pour un JobRepository persistant (sinon, H2 embarqué est suffisant).

---

## Installation & Compilation

1. **Cloner** ou télécharger ce dépôt :
   ```bash
   git clone https://github.com/.../caisse-retraite-batch.git
   ```
2. Se placer dans le répertoire du projet et compiler avec Maven :
   ```bash
   cd caisse-retraite-batch
   mvn clean install
   ```

---

## Configuration

Les paramètres de configuration se trouvent dans le fichier :
```
src/main/resources/application.properties
```
Vous pouvez adapter notamment :
- Le seuil d’erreurs pour la **Skip Policy**
- Le mode d’écriture (multi-onglets ou multi-fichiers)
- Les paramètres de lecture du CSV

---

## Exécution

Après compilation, lancez l’application avec la commande suivante :
```bash
java -jar target/caisse-retraite-batch.jar
```
Le traitement du fichier CSV s’effectuera selon la configuration définie dans `application.properties`.

---

## Logs & Monitoring

- **Logs** : Les logs générés par Spring Boot et Spring Batch permettent de suivre l’exécution du job.
- **Monitoring** : L’historique des jobs est stocké dans les tables `BATCH_*`, facilitant le suivi et l’analyse.

---

## Structure du projet

```
caisse-retraite-batch
├── pom.xml
└── src
    └── main
        ├── java
        │   └── com.example.caisseretraitebatch
        │       ├── CaisseRetraiteBatchApplication.java
        │       ├── config
        │       │   ├── BatchConfig.java           // Configuration principale Spring Batch
        │       │   └── MySkipPolicy.java          // Skip policy personnalisé (ex : 50 lignes)
        │       ├── model
        │       │   └── CaisseRetraiteRecord.java  // Entité pour les données CSV (11 colonnes)
        │       ├── processor
        │       │   └── BeanValidationProcessor.java // Processor JSR‑303
        │       └── writer
        │           ├── MultiSheetExcelItemWriter.java // Writer multi-onglets
        │           └── MultiFileExcelItemWriter.java    // Writer multi-fichiers
        └── resources
            └── application.properties
```

---

## Classes principales

- **`CaisseRetraiteBatchApplication`** : Point d’entrée de l’application Spring Boot.
- **`BatchConfig`** : Déclaration du Reader CSV, du Processor de validation, des Writers Excel, ainsi que du Step et du Job.
- **`CaisseRetraiteRecord`** : Modèle de données pour le CSV, avec annotations JSR‑303.
- **`BeanValidationProcessor`** : Applique la validation sur chaque ligne du CSV.
- **`MySkipPolicy`** : Gère le nombre maximal d’erreurs (ex. 50) avant l’arrêt du job.
- **`MultiSheetExcelItemWriter`** : Génère plusieurs onglets dans un seul fichier Excel.
- **`MultiFileExcelItemWriter`** : Génère plusieurs fichiers Excel si le seuil de lignes est dépassé.

---

## Approfondissements techniques

Le projet peut être étendu pour :
- Gérer un très grand nombre de colonnes.
- Implémenter des validations complexes.
- Intégrer un monitoring avancé avec Spring Batch et d’autres outils de suivi.

---

## Personnalisation

Les comportements du batch peuvent être personnalisés via :
- Le fichier `application.properties` (pour adapter la configuration globale).
- La configuration dans `BatchConfig.java` et l’implémentation personnalisée de la **Skip Policy**.
