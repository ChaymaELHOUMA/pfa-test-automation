const express = require('express');
const router = require('./routes-utilisateurs');

const app = express();
app.use(express.json());

// Montage du routeur principal
app.use(router);

// Middleware de gestion des routes 404 non trouvees
app.use((req, res) => {
  res.status(404).json({ error: 'Ressource introuvable' });
});

// Middleware de gestion d'erreur global (500) pour intercepter les exceptions 'throw'
app.use((err, req, res, next) => {
  res.status(500).json({ error: err.message || 'Erreur interne du serveur' });
});

module.exports = app;
