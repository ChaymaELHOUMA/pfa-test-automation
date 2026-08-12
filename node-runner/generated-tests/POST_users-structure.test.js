const express = require('express');
const request = require('supertest');

// --- Définition du handler ---

// Fonction de validation simulée pour l'exercice
const validerEmail = (email) => typeof email === 'string' && email.includes('@');

const app = express();
app.use(express.json());

app.post('/users', (req, res) => {
  if (!req.body.email || !validerEmail(req.body.email)) {
    return res.status(400).json({ error: 'Email invalide' });
  }

  try {
    // Simulation d'une erreur pour le test de bloc try/catch
    if (req.body.email === 'trigger-error@test.com') {
      throw new Error('Erreur base de données');
    }
    const nouvelUtilisateur = { id: 1, email: req.body.email };
    res.status(201).json(nouvelUtilisateur);
  } catch (err) {
    res.status(500).json({ error: 'Erreur serveur' });
  }
});

// --- Tests ---

describe('POST /users', () => {
  // 1. Cas nominal
  test('devrait retourner 201 pour un email valide', async () => {
    const response = await request(app)
      .post('/users')
      .send({ email: 'test@example.com' });
    expect(response.status).toBe(201);
    expect(response.body).toEqual({ id: 1, email: 'test@example.com' });
  });

  // 2 & 3. Cas où la condition if est vraie et renvoie 400
  test('devrait retourner 400 si l\'email est manquant', async () => {
    const response = await request(app).post('/users').send({});
    expect(response.status).toBe(400);
    expect(response.body.error).toBe('Email invalide');
  });

  test('devrait retourner 400 si l\'email est invalide', async () => {
    const response = await request(app)
      .post('/users')
      .send({ email: 'invalid-email' });
    expect(response.status).toBe(400);
    expect(response.body.error).toBe('Email invalide');
  });

  // 4 & 5. Cas où une exception est levée dans le try et renvoie 500
  test('devrait retourner 500 en cas d\'exception dans le bloc try', async () => {
    const response = await request(app)
      .post('/users')
      .send({ email: 'trigger-error@test.com' });
    expect(response.status).toBe(500);
    expect(response.body.error).toBe('Erreur serveur');
  });
});