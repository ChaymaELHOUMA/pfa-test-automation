const express = require('express');
const request = require('supertest');

// Définition de la fonction de validation (pour rendre le test autonome)
const validerEmail = (email) => {
  return typeof email === 'string' && email.includes('@');
};

// Définition du handler de route
const router = express.Router();
router.post('/users', (req, res) => {
  if (!req.body.email || !validerEmail(req.body.email)) {
    return res.status(400).json({ error: 'Email invalide' });
  }

  try {
    const nouvelUtilisateur = { id: 1, email: req.body.email };
    res.status(201).json(nouvelUtilisateur);
  } catch (err) {
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

// Configuration de l'application de test
const app = express();
app.use(express.json());
app.use(router);

// Tests Jest
describe('POST /users', () => {
  test('devrait créer un utilisateur avec un email valide (cas nominal)', async () => {
    const response = await request(app)
      .post('/users')
      .send({ email: 'test@example.com' });

    expect(response.status).toBe(201);
    expect(response.body).toEqual({
      id: 1,
      email: 'test@example.com'
    });
  });

  test('devrait retourner 400 si l\'email est invalide', async () => {
    const response = await request(app)
      .post('/users')
      .send({ email: 'invalid-email' });

    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Email invalide' });
  });

  test('devrait retourner 400 si l\'email est manquant', async () => {
    const response = await request(app)
      .post('/users')
      .send({});

    expect(response.status).toBe(400);
    expect(response.body).toEqual({ error: 'Email invalide' });
  });
});