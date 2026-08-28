const express = require("express");
const router = express.Router();
const userModel = require("../src/userModel");
const { isValidEmail } = require("../src/utils");

// GET /api/users - Liste tous les utilisateurs
router.get("/", (req, res) => {
  res.status(200).json(userModel.getAll());
});

// GET /api/users/:id - Récupère un utilisateur par id
router.get("/:id", (req, res) => {
  const id = Number(req.params.id);
  if (Number.isNaN(id)) {
    return res.status(400).json({ error: "id invalide" });
  }
  const user = userModel.getById(id);
  if (!user) {
    return res.status(404).json({ error: "Utilisateur non trouvé" });
  }
  res.status(200).json(user);
});

// POST /api/users - Crée un nouvel utilisateur
router.post("/", (req, res) => {
  const { name, email } = req.body;
  if (!name || !email) {
    return res.status(400).json({ error: "name et email sont requis" });
  }
  if (!isValidEmail(email)) {
    return res.status(400).json({ error: "email invalide" });
  }
  const newUser = userModel.create({ name, email });
  res.status(201).json(newUser);
});

// PUT /api/users/:id - Met à jour un utilisateur
router.put("/:id", (req, res) => {
  const id = Number(req.params.id);
  if (Number.isNaN(id)) {
    return res.status(400).json({ error: "id invalide" });
  }
  const { name, email } = req.body;
  if (email !== undefined && !isValidEmail(email)) {
    return res.status(400).json({ error: "email invalide" });
  }
  const updated = userModel.update(id, { name, email });
  if (!updated) {
    return res.status(404).json({ error: "Utilisateur non trouvé" });
  }
  res.status(200).json(updated);
});

// DELETE /api/users/:id - Supprime un utilisateur
router.delete("/:id", (req, res) => {
  const id = Number(req.params.id);
  if (Number.isNaN(id)) {
    return res.status(400).json({ error: "id invalide" });
  }
  const deleted = userModel.remove(id);
  if (!deleted) {
    return res.status(404).json({ error: "Utilisateur non trouvé" });
  }
  res.status(204).send();
});

module.exports = router;
