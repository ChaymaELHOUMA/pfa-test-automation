# Express Simple App

Projet Express.js simple, pensé comme **cible** pour une application web de génération de tests. Aucun test n'est inclus — l'API expose une petite logique métier claire et bien séparée, prête à être analysée et testée automatiquement par votre outil.

## Structure

```
express-app/
├── app.js                 # Configuration de l'app Express (exportée, sans listen())
├── server.js               # Point d'entrée, démarre le serveur
├── package.json
├── routes/
│   ├── users.js            # CRUD /api/users
│   └── calculator.js       # /api/calculator/{add,subtract,multiply,divide}
└── src/
    ├── userModel.js        # "Base de données" en mémoire (avec reset())
    └── utils.js             # Fonctions pures (add, subtract, isValidEmail, ...)
```

## Installation

```bash
npm install
```

## Lancer le serveur

```bash
npm start
# ou avec rechargement automatique
npm run dev
```

Le serveur écoute par défaut sur `http://localhost:3000`.

## Endpoints disponibles

| Méthode | Route                      | Description                     |
|---------|-----------------------------|----------------------------------|
| GET     | `/`                          | Message de bienvenue            |
| GET     | `/health`                    | Vérification de l'état du serveur|
| GET     | `/api/users`                 | Liste tous les utilisateurs     |
| GET     | `/api/users/:id`             | Récupère un utilisateur         |
| POST    | `/api/users`                 | Crée un utilisateur (`name`, `email`) |
| PUT     | `/api/users/:id`             | Met à jour un utilisateur       |
| DELETE  | `/api/users/:id`             | Supprime un utilisateur         |
| GET     | `/api/calculator/add?a=&b=`  | Additionne deux nombres         |
| GET     | `/api/calculator/subtract?a=&b=` | Soustrait deux nombres      |
| GET     | `/api/calculator/multiply?a=&b=` | Multiplie deux nombres      |
| GET     | `/api/calculator/divide?a=&b=`   | Divise deux nombres (400 si b=0) |

## Pourquoi cette structure est adaptée à la génération de tests

- **`app.js` exporte l'application Express sans appeler `listen()`**, ce qui permet à un outil (ex. Supertest) de l'importer et de lui envoyer des requêtes HTTP simulées.
- **La logique métier est séparée** dans `src/` (fonctions pures et modèle de données), facile à cibler avec des tests unitaires isolés.
- **Les routes sont séparées par domaine** (`users`, `calculator`), pour générer des suites de tests indépendantes par fichier.
- **`userModel.reset()`** permet de réinitialiser l'état entre chaque test généré, évitant les effets de bord.
- **Des cas d'erreurs explicites** (400, 404, 500) offrent des cas limites clairs à couvrir.
