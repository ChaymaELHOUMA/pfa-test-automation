# PFA Test Automation

## Vue d'ensemble

PFA Test Automation est une plateforme complète d'automatisation de tests qui orchestre la génération de tests JavaScript par intelligence artificielle. Le système fait le pont entre un backend Java Spring Boot et des environnements d'exécution Node.js pour générer, exécuter et analyser automatiquement des tests JavaScript.

## Architecture

Le projet se compose de trois composants principaux :

- **Backend** : Application Java Spring Boot (port 8080)
- **Frontend** : Application React TypeScript avec Vite
- **Node Runner** : Environnement d'exécution Node.js pour l'exécution des tests

## Fonctionnalités

- **Analyse AST** : Analyser les arbres de syntaxe abstraite JavaScript
- **Génération de tests par IA** : Générer des tests Jest en utilisant des LLM
- **Tests multi-types** : Support pour les tests unitaires, d'intégration et E2E
- **Analyse de couverture** : Rapports de couverture Istanbul/NYC
- **Tests de mutation** : Intégration des tests de mutation Stryker
- **Déduplication de tests** : Déduplication intelligente des tests
- **Gestion de projets** : Télécharger et gérer plusieurs projets JavaScript

## Prérequis

- Java 17 ou supérieur
- Node.js 18+
- Maven 3.6+
- npm ou yarn
- Clé API LLM (configurée dans .env)

## Installation

### 1. Cloner le dépôt

```bash
git clone <url-du-dépôt>
cd pfa-test-automation
```

### 2. Configuration du Backend

```bash
# Installer les dépendances (niveau racine pour le pont Node)
npm install

# Configurer les variables d'environnement
cp .env.example .env
# Éditez .env avec votre clé API LLM et configuration

# Construire l'application Spring Boot
mvn clean install

# Lancer le backend
mvn spring-boot:run
```

Le backend démarrera sur `http://localhost:8080`

### 3. Configuration du Frontend

```bash
cd frontend

# Installer les dépendances
npm install

# Lancer le serveur de développement
npm run dev
```

Le frontend démarrera sur `http://localhost:5173`

### 4. Configuration du Node Runner

```bash
cd node-runner

# Installer les dépendances
npm install

# Installer les dépendances de tests globalement
npm install -g jest npx stryker-cli
```

## Configuration

### Variables d'environnement (.env)

```env
# Configuration LLM
LLM_API_KEY=votre_clé_api_ici
LLM_API_MODEL=gemini-1.5-flash
LLM_API_TIMEOUT_SECONDS=20

# Configuration du pont Node
NODE_BRIDGE_TIMEOUT_SECONDS=30
NODE_BRIDGE_SCRIPTS_DIR=node-runner

# Configuration du serveur
SERVER_PORT=8080
```

### Propriétés de l'application

L'application Spring Boot utilise `application.properties` pour une configuration supplémentaire :

```properties
server.port=8080
spring.application.name=test-automation
```

## Utilisation

### Démarrage de l'application

1. Démarrer le backend : `mvn spring-boot:run`
2. Démarrer le frontend : `cd frontend && npm run dev`
3. Accéder à l'application sur `http://localhost:5173`

### Endpoints API

#### Gestion des projets
- `GET /api/projets` - Lister tous les projets
- `POST /upload-projet` - Télécharger un nouveau projet (fichier ZIP)

#### Analyse AST
- `GET /analyser-ast` - Analyser l'AST JavaScript

#### Génération de tests
- `GET /construire-prompt` - Construire le prompt de génération de tests
- `POST /generer-et-executer-test` - Générer et exécuter des tests
- `POST /generer-suite-multi-type` - Générer une suite de tests multi-types

#### Tests et analyse
- `GET /api/coverage/{idProjet}` - Obtenir les métriques de couverture
- `GET /api/mutation/{idProjet}` - Obtenir les résultats des tests de mutation
- `POST /valider-non-redondance` - Valider la déduplication de tests

## Structure du projet

```
pfa-test-automation/
├── src/
│   ├── main/
│   │   ├── java/com/smartautomation/testautomation/
│   │   │   ├── controller/      # Contrôleurs REST
│   │   │   ├── service/         # Logique métier
│   │   │   ├── model/           # Modèles de données
│   │   │   ├── config/          # Classes de configuration
│   │   │   └── exception/       # Exceptions personnalisées
│   │   └── resources/           # Fichiers de configuration
│   └── test/                    # Tests backend
├── frontend/
│   ├── src/
│   │   ├── components/          # Composants React
│   │   ├── services/            # Services API
│   │   ├── types/               # Types TypeScript
│   │   └── views/               # Vues de pages
│   └── package.json
├── node-runner/                 # Environnement d'exécution Node.js
│   ├── workspaces/              # Espaces de travail de projets
│   ├── generated-tests/         # Fichiers de tests générés
│   └── projet-reference/        # Projet de référence
├── pom.xml                      # Configuration Maven
└── README.md
```

## Développement

### Développement Backend

```bash
# Exécuter les tests
mvn test

# Exécuter un test spécifique
mvn test -Dtest=NodeBridgeServiceTest

# Construire sans tests
mvn clean install -DskipTests
```

### Développement Frontend

```bash
cd frontend

# Lancer le serveur de développement
npm run dev

# Construire pour la production
npm run build

# Exécuter le linter
npm run lint

# Prévisualiser le build de production
npm run preview
```

## Tests

### Tests Backend

Le projet inclut des tests backend complets :

- Tests unitaires pour les services (NodeBridgeService, ProjectService, etc.)
- Tests d'intégration pour les contrôleurs
- Tests de bout en bout pour les pipelines

Exécuter les tests avec : `mvn test`

### Tests Frontend

Les tests frontend doivent être ajoutés en utilisant React Testing Library :

```bash
cd frontend
npm test
```

