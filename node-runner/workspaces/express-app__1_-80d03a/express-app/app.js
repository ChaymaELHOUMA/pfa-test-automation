const express = require("express");
const usersRouter = require("./routes/users");
const calculatorRouter = require("./routes/calculator");

const app = express();

app.use(express.json());

// Route de santé, utile pour vérifier que le serveur tourne
app.get("/", (req, res) => {
  res.status(200).json({ message: "Bienvenue sur l'API Express simple" });
});

app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});

app.use("/api/users", usersRouter);
app.use("/api/calculator", calculatorRouter);

// Gestion des routes non trouvées
app.use((req, res) => {
  res.status(404).json({ error: "Route non trouvée" });
});

// Gestion centralisée des erreurs
app.use((err, req, res, next) => {
  console.error(err.stack);
  res.status(500).json({ error: "Erreur interne du serveur" });
});

module.exports = app;
// Note : app.js n'appelle pas listen() volontairement.
// Cela permet à un outil externe (ex. Supertest) d'importer `app`
// et de lui envoyer des requêtes HTTP simulées sans ouvrir de vrai port.
